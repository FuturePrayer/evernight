package cn.suhoan.evernight.client;

import cn.suhoan.evernight.cache.EvernightCacheService;
import cn.suhoan.evernight.config.CargoRegistryProperties;
import cn.suhoan.evernight.exception.ExternalServiceException;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.model.CargoCrateInfo;
import cn.suhoan.evernight.model.CargoCrateSearchResult;
import cn.suhoan.evernight.model.CargoCrateSummary;
import cn.suhoan.evernight.model.CargoCrateVersionDetail;
import cn.suhoan.evernight.model.CargoCrateVersionSummary;
import cn.suhoan.evernight.model.CargoDependencyInfo;
import cn.suhoan.evernight.support.InputValidator;
import cn.suhoan.evernight.support.JsonSupport;
import cn.suhoan.evernight.whitelist.CargoRegistryResolver;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@EnableConfigurationProperties(CargoRegistryProperties.class)
public class CargoCrateClient {

    private static final Logger log = LoggerFactory.getLogger(CargoCrateClient.class);

    private static final String RUST_VERSION_NOTE = "rustVersion 来自 crate 元数据声明，表示最低 Rust 版本或兼容要求，不代表实际编译时使用的 rustc 版本。";

    private static final Map<String, String> JSON_HEADERS = Map.of("Accept", "application/json");

    private final ExternalHttpClient httpClient;

    private final JsonSupport jsonSupport;

    private final EvernightCacheService cacheService;

    private final CargoRegistryResolver registryResolver;

    public CargoCrateClient(ExternalHttpClient httpClient, JsonSupport jsonSupport, EvernightCacheService cacheService,
            CargoRegistryResolver registryResolver) {
        this.httpClient = httpClient;
        this.jsonSupport = jsonSupport;
        this.cacheService = cacheService;
        this.registryResolver = registryResolver;
    }

    public CargoCrateSearchResult search(String keyword, Integer perPage, String registryBaseUrl) {
        String effectiveRegistryBaseUrl = registryResolver.resolve(registryBaseUrl);
        String cacheKey = "search:" + effectiveRegistryBaseUrl + ":" + keyword + ":" + perPage;
        return cacheService.get("cargo", cacheKey, cacheService.properties().getCargoTtlSeconds(),
                () -> doSearch(keyword, perPage, effectiveRegistryBaseUrl));
    }

    CargoCrateSearchResult doSearch(String keyword, Integer perPage, String registryBaseUrl) {
        String normalizedKeyword = InputValidator.requireText(keyword, "keyword");
        int limit = InputValidator.clampPageSize(perPage, 20, 50);
        log.info("开始搜索 Cargo crate，keyword={}，perPage={}，registry={}", normalizedKeyword, limit, registryBaseUrl);
        JsonNode root = getJson(buildSearchUrl(registryBaseUrl, normalizedKeyword, limit), "请求 Cargo registry 搜索 API 失败");
        List<CargoCrateSummary> crates = new ArrayList<>();
        for (JsonNode crateNode : root.path("crates")) {
            crates.add(toSummary(crateNode));
        }
        long total = root.path("meta").path("total").asLong(crates.size());
        log.info("Cargo crate 搜索完成，keyword={}，total={}，returned={}", normalizedKeyword, total, crates.size());
        return new CargoCrateSearchResult(total, registryBaseUrl, List.copyOf(crates));
    }

    public CargoCrateInfo crateInfo(String crateName, Integer versionLimit, String registryBaseUrl) {
        String effectiveRegistryBaseUrl = registryResolver.resolve(registryBaseUrl);
        String cacheKey = "info:" + effectiveRegistryBaseUrl + ":" + crateName + ":" + versionLimit;
        return cacheService.get("cargo", cacheKey, cacheService.properties().getCargoTtlSeconds(),
                () -> doCrateInfo(crateName, versionLimit, effectiveRegistryBaseUrl));
    }

    CargoCrateInfo doCrateInfo(String crateName, Integer versionLimit, String registryBaseUrl) {
        String normalizedName = InputValidator.requireText(crateName, "crateName");
        int limit = InputValidator.clampPageSize(versionLimit, 20, 100);
        log.info("开始查询 Cargo crate 信息，crateName={}，versionLimit={}，registry={}", normalizedName, limit, registryBaseUrl);
        JsonNode root = getJson(buildCrateUrl(registryBaseUrl, normalizedName), "请求 Cargo registry crate API 失败");
        JsonNode crateNode = root.path("crate");
        List<CargoCrateVersionSummary> versions = new ArrayList<>();
        for (JsonNode versionNode : root.path("versions")) {
            if (versions.size() >= limit) {
                break;
            }
            versions.add(toVersionSummary(versionNode));
        }
        CargoCrateInfo info = new CargoCrateInfo(textOrDefault(crateNode.path("name"), normalizedName),
                textOrNull(crateNode.path("description")), textOrNull(crateNode.path("max_version")),
                textOrNull(crateNode.path("max_stable_version")), registryBaseUrl, textOrNull(crateNode.path("license")),
                textOrNull(crateNode.path("homepage")), textOrNull(crateNode.path("documentation")),
                textOrNull(crateNode.path("repository")), crateNode.path("downloads").asLong(0),
                crateNode.path("recent_downloads").asLong(0), toStringList(root.path("keywords")),
                readCategoryNames(root.path("categories")), List.copyOf(versions));
        log.info("Cargo crate 信息查询完成，crateName={}，latestVersion={}，versionCount={}",
                normalizedName, info.latestVersion(), info.versions().size());
        return info;
    }

    public CargoCrateVersionDetail versionDetail(String crateName, String version, String registryBaseUrl) {
        String effectiveRegistryBaseUrl = registryResolver.resolve(registryBaseUrl);
        String cacheKey = "version-detail:" + effectiveRegistryBaseUrl + ":" + crateName + ":" + version;
        return cacheService.get("cargo", cacheKey, cacheService.properties().getCargoTtlSeconds(),
                () -> doVersionDetail(crateName, version, effectiveRegistryBaseUrl));
    }

    CargoCrateVersionDetail doVersionDetail(String crateName, String version, String registryBaseUrl) {
        String normalizedName = InputValidator.requireText(crateName, "crateName");
        String normalizedVersion = StringUtils.hasText(version) ? version.trim() : null;
        if (!StringUtils.hasText(normalizedVersion)) {
            normalizedVersion = readDefaultVersion(registryBaseUrl, normalizedName);
        }
        log.info("开始查询 Cargo crate 版本详情，crateName={}，version={}，registry={}",
                normalizedName, normalizedVersion, registryBaseUrl);
        JsonNode root = getJson(buildVersionUrl(registryBaseUrl, normalizedName, normalizedVersion),
                "请求 Cargo registry 版本 API 失败");
        JsonNode versionNode = root.path("version");
        List<CargoDependencyInfo> dependencies = readDependencies(root.path("dependencies"));
        if (dependencies.isEmpty()) {
            dependencies = fetchDependencies(registryBaseUrl, normalizedName, normalizedVersion);
        }
        CargoCrateVersionDetail detail = new CargoCrateVersionDetail(textOrDefault(versionNode.path("crate"), normalizedName),
                textOrDefault(versionNode.path("num"), normalizedVersion), registryBaseUrl, textOrNull(versionNode.path("description")),
                textOrNull(versionNode.path("license")), textOrNull(versionNode.path("homepage")),
                textOrNull(versionNode.path("documentation")), textOrNull(versionNode.path("repository")),
                textOrNull(versionNode.path("rust_version")), textOrNull(versionNode.path("edition")),
                textOrNull(versionNode.path("checksum")), versionNode.path("yanked").asBoolean(false),
                textOrNull(versionNode.path("yank_message")), versionNode.path("crate_size").asLong(0),
                versionNode.path("downloads").asLong(0), textOrNull(versionNode.path("created_at")),
                textOrNull(versionNode.path("updated_at")), readFeatures(versionNode.path("features")),
                List.copyOf(dependencies), RUST_VERSION_NOTE);
        log.info("Cargo crate 版本详情查询完成，crateName={}，version={}，dependencyCount={}",
                normalizedName, normalizedVersion, detail.dependencies().size());
        return detail;
    }

    private String readDefaultVersion(String registryBaseUrl, String crateName) {
        JsonNode root = getJson(buildCrateUrl(registryBaseUrl, crateName), "请求 Cargo registry crate API 失败");
        JsonNode crateNode = root.path("crate");
        String version = textOrNull(crateNode.path("max_stable_version"));
        if (!StringUtils.hasText(version)) {
            version = textOrNull(crateNode.path("max_version"));
        }
        if (!StringUtils.hasText(version)) {
            version = textOrNull(crateNode.path("newest_version"));
        }
        if (!StringUtils.hasText(version)) {
            throw new IllegalArgumentException("Cargo crate 缺少可用版本: " + crateName);
        }
        return version;
    }

    private List<CargoDependencyInfo> fetchDependencies(String registryBaseUrl, String crateName, String version) {
        JsonNode root = getJson(buildDependenciesUrl(registryBaseUrl, crateName, version),
                "请求 Cargo registry 依赖 API 失败");
        return readDependencies(root.path("dependencies"));
    }

    private JsonNode getJson(String url, String errorMessage) {
        ExternalHttpResponse response = httpClient.get(url, JSON_HEADERS);
        if (response.isNotFound()) {
            throw new IllegalArgumentException("未找到 Cargo crate 资源: " + url);
        }
        if (!response.isSuccess()) {
            throw new ExternalServiceException(errorMessage + "，status=" + response.statusCode());
        }
        return jsonSupport.readTree(response.body());
    }

    static String buildSearchUrl(String registryBaseUrl, String keyword, int perPage) {
        return registryBaseUrl + "/crates?q=" + encode(keyword) + "&per_page=" + perPage;
    }

    static String buildCrateUrl(String registryBaseUrl, String crateName) {
        return registryBaseUrl + "/crates/" + encodePath(crateName);
    }

    static String buildVersionUrl(String registryBaseUrl, String crateName, String version) {
        return buildCrateUrl(registryBaseUrl, crateName) + "/" + encodePath(version);
    }

    static String buildDependenciesUrl(String registryBaseUrl, String crateName, String version) {
        return buildVersionUrl(registryBaseUrl, crateName, version) + "/dependencies";
    }

    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String encodePath(String value) {
        return encode(value);
    }

    private static CargoCrateSummary toSummary(JsonNode node) {
        return new CargoCrateSummary(textOrDefault(node.path("name"), textOrNull(node.path("id"))),
                textOrNull(node.path("max_version")), textOrNull(node.path("newest_version")),
                textOrNull(node.path("max_stable_version")), textOrNull(node.path("description")),
                textOrNull(node.path("homepage")), textOrNull(node.path("documentation")),
                textOrNull(node.path("repository")), node.path("downloads").asLong(0),
                node.path("recent_downloads").asLong(0), node.path("num_versions").asInt(0),
                node.path("yanked").asBoolean(false));
    }

    private static CargoCrateVersionSummary toVersionSummary(JsonNode node) {
        return new CargoCrateVersionSummary(textOrNull(node.path("num")), node.path("yanked").asBoolean(false),
                textOrNull(node.path("license")), textOrNull(node.path("rust_version")),
                textOrNull(node.path("created_at")), textOrNull(node.path("updated_at")),
                node.path("downloads").asLong(0), node.path("crate_size").asLong(0));
    }

    private static List<CargoDependencyInfo> readDependencies(JsonNode dependenciesNode) {
        List<CargoDependencyInfo> dependencies = new ArrayList<>();
        if (dependenciesNode != null && dependenciesNode.isArray()) {
            for (JsonNode dependencyNode : dependenciesNode) {
                dependencies.add(new CargoDependencyInfo(textOrNull(dependencyNode.path("crate_id")),
                        textOrNull(dependencyNode.path("req")), textOrNull(dependencyNode.path("kind")),
                        dependencyNode.path("optional").asBoolean(false),
                        dependencyNode.path("default_features").asBoolean(false),
                        toStringList(dependencyNode.path("features")), textOrNull(dependencyNode.path("target")),
                        dependencyNode.path("downloads").asLong(0)));
            }
        }
        return List.copyOf(dependencies);
    }

    private static Map<String, List<String>> readFeatures(JsonNode featuresNode) {
        Map<String, List<String>> features = new LinkedHashMap<>();
        if (featuresNode != null && featuresNode.isObject()) {
            for (Map.Entry<String, JsonNode> field : featuresNode.properties()) {
                features.put(field.getKey(), toStringList(field.getValue()));
            }
        }
        return Map.copyOf(features);
    }

    private static List<String> readCategoryNames(JsonNode categoriesNode) {
        List<String> categories = new ArrayList<>();
        if (categoriesNode != null && categoriesNode.isArray()) {
            for (JsonNode categoryNode : categoriesNode) {
                String category = textOrNull(categoryNode.path("category"));
                if (!StringUtils.hasText(category)) {
                    category = textOrNull(categoryNode.path("slug"));
                }
                if (StringUtils.hasText(category)) {
                    categories.add(category);
                }
            }
        }
        return List.copyOf(categories);
    }

    private static List<String> toStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    values.add(item.asText());
                }
                else if (item.has("keyword")) {
                    values.add(item.path("keyword").asText());
                }
                else if (item.has("id")) {
                    values.add(item.path("id").asText());
                }
                else {
                    values.add(item.asText());
                }
            }
        }
        return List.copyOf(values);
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private static String textOrDefault(JsonNode node, String defaultValue) {
        String value = textOrNull(node);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

}
