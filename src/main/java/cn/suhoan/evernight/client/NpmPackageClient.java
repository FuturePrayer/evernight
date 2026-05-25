package cn.suhoan.evernight.client;


import cn.suhoan.evernight.config.NpmRegistryProperties;
import cn.suhoan.evernight.model.NpmPackageInfo;
import cn.suhoan.evernight.whitelist.NpmRegistryResolver;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.suhoan.evernight.cache.EvernightCacheService;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.exception.ExternalServiceException;
import cn.suhoan.evernight.support.InputValidator;
import cn.suhoan.evernight.support.JsonSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(NpmRegistryProperties.class)
public class NpmPackageClient {

    private static final Logger log = LoggerFactory.getLogger(NpmPackageClient.class);

    private final ExternalHttpClient httpClient;

    private final JsonSupport jsonSupport;

    private final EvernightCacheService cacheService;

    private final NpmRegistryResolver registryResolver;

    public NpmPackageClient(ExternalHttpClient httpClient, JsonSupport jsonSupport, EvernightCacheService cacheService,
            NpmRegistryResolver registryResolver) {
        this.httpClient = httpClient;
        this.jsonSupport = jsonSupport;
        this.cacheService = cacheService;
        this.registryResolver = registryResolver;
    }

    public NpmPackageInfo packageInfo(String packageName, Integer versionLimit) {
        return packageInfo(packageName, versionLimit, null);
    }

    public NpmPackageInfo packageInfo(String packageName, Integer versionLimit, String registryBaseUrl) {
        String effectiveRegistryBaseUrl = registryResolver.resolve(registryBaseUrl);
        String cacheKey = effectiveRegistryBaseUrl + ":" + packageName + ":" + versionLimit;
        return cacheService.get("npm", cacheKey, cacheService.properties().getNpmTtlSeconds(),
                () -> doPackageInfo(packageName, versionLimit, effectiveRegistryBaseUrl));
    }

    NpmPackageInfo doPackageInfo(String packageName, Integer versionLimit, String registryBaseUrl) {
        String normalizedName = InputValidator.requireText(packageName, "packageName");
        int limit = InputValidator.clampPageSize(versionLimit, 20, 100);
        log.info("开始查询 npm 包信息，packageName={}，versionLimit={}，registry={}", normalizedName, limit, registryBaseUrl);
        ExternalHttpResponse response = httpClient.get(buildPackageUrl(registryBaseUrl, normalizedName), Map.of("Accept", "application/json"));
        if (response.isNotFound()) {
            throw new IllegalArgumentException("未找到 npm 包: " + normalizedName);
        }
        if (!response.isSuccess()) {
            throw new ExternalServiceException("请求 npm registry 失败，status=" + response.statusCode());
        }
        JsonNode root = jsonSupport.readTree(response.body());
        String latest = root.path("dist-tags").path("latest").asText(null);
        JsonNode latestVersion = root.path("versions").path(latest);
        NpmPackageInfo info = new NpmPackageInfo(root.path("name").asText(normalizedName), root.path("description").asText(null), latest,
                registryBaseUrl, toStringMap(root.path("dist-tags")), textOrNull(latestVersion.path("license")),
                textOrNull(root.path("homepage")), readRepository(root), toStringMap(latestVersion.path("dependencies")),
                limitedFieldNames(root.path("versions"), limit));
        log.info("npm 包信息查询完成，packageName={}，latestVersion={}，versionCount={}", normalizedName, latest, info.versions().size());
        return info;
    }

    static String encodePackageName(String packageName) {
        // npm scoped 包名中的斜杠需要编码成 %2F。
        return URLEncoder.encode(packageName, StandardCharsets.UTF_8).replace("+", "%20");
    }

    static String buildPackageUrl(String registryBaseUrl, String packageName) {
        return registryBaseUrl + "/" + encodePackageName(packageName);
    }

    private static String readRepository(JsonNode root) {
        JsonNode repository = root.path("repository");
        if (repository.isTextual()) {
            return repository.asText();
        }
        return textOrNull(repository.path("url"));
    }

    private static Map<String, String> toStringMap(JsonNode node) {
        Map<String, String> values = new LinkedHashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                values.put(field.getKey(), field.getValue().asText());
            }
        }
        return values;
    }

    private static List<String> limitedFieldNames(JsonNode node, int limit) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        if (node != null && node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext() && names.size() < limit) {
                names.add(fieldNames.next());
            }
        }
        return List.copyOf(names);
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }

}
