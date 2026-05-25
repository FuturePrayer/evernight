package cn.suhoan.evernight.client;


import cn.suhoan.evernight.config.PypiRepositoryProperties;
import cn.suhoan.evernight.model.PypiPackageInfo;
import cn.suhoan.evernight.model.PypiReleaseFile;
import cn.suhoan.evernight.model.PypiReleaseFiles;
import cn.suhoan.evernight.whitelist.PypiRepositoryResolver;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
import org.springframework.util.StringUtils;

@Service
@EnableConfigurationProperties(PypiRepositoryProperties.class)
public class PypiPackageClient {

    private static final Logger log = LoggerFactory.getLogger(PypiPackageClient.class);

    private final ExternalHttpClient httpClient;

    private final JsonSupport jsonSupport;

    private final EvernightCacheService cacheService;

    private final PypiRepositoryResolver repositoryResolver;

    public PypiPackageClient(ExternalHttpClient httpClient, JsonSupport jsonSupport, EvernightCacheService cacheService,
            PypiRepositoryResolver repositoryResolver) {
        this.httpClient = httpClient;
        this.jsonSupport = jsonSupport;
        this.cacheService = cacheService;
        this.repositoryResolver = repositoryResolver;
    }

    public PypiPackageInfo packageInfo(String packageName, Integer releaseLimit) {
        return packageInfo(packageName, releaseLimit, null);
    }

    public PypiPackageInfo packageInfo(String packageName, Integer releaseLimit, String repositoryBaseUrl) {
        String effectiveRepositoryBaseUrl = repositoryResolver.resolve(repositoryBaseUrl);
        String cacheKey = effectiveRepositoryBaseUrl + ":" + packageName + ":" + releaseLimit;
        return cacheService.get("pypi", cacheKey, cacheService.properties().getPypiTtlSeconds(),
                () -> doPackageInfo(packageName, releaseLimit, effectiveRepositoryBaseUrl));
    }

    public PypiReleaseFiles releaseFiles(String packageName, String version, String repositoryBaseUrl) {
        String effectiveRepositoryBaseUrl = repositoryResolver.resolve(repositoryBaseUrl);
        String cacheKey = "release-files:" + effectiveRepositoryBaseUrl + ":" + packageName + ":" + version;
        return cacheService.get("pypi", cacheKey, cacheService.properties().getPypiTtlSeconds(),
                () -> doReleaseFiles(packageName, version, effectiveRepositoryBaseUrl));
    }

    PypiPackageInfo doPackageInfo(String packageName, Integer releaseLimit, String repositoryBaseUrl) {
        String normalizedName = InputValidator.requireText(packageName, "packageName");
        int limit = InputValidator.clampPageSize(releaseLimit, 20, 100);
        log.info("开始查询 PyPI 包信息，packageName={}，releaseLimit={}，repository={}", normalizedName, limit, repositoryBaseUrl);
        JsonNode root = fetchPackageJson(repositoryBaseUrl, normalizedName);
        JsonNode info = root.path("info");
        PypiPackageInfo packageInfo = new PypiPackageInfo(info.path("name").asText(normalizedName), textOrNull(info.path("summary")),
                textOrNull(info.path("version")), repositoryBaseUrl, textOrNull(info.path("requires_python")),
                textOrNull(info.path("license")), textOrNull(info.path("home_page")), toStringList(info.path("classifiers")),
                toStringMap(info.path("project_urls")), limitedFieldNames(root.path("releases"), limit));
        log.info("PyPI 包信息查询完成，packageName={}，latestVersion={}，releaseCount={}", normalizedName, packageInfo.latestVersion(), packageInfo.releases().size());
        return packageInfo;
    }

    PypiReleaseFiles doReleaseFiles(String packageName, String version, String repositoryBaseUrl) {
        String normalizedName = InputValidator.requireText(packageName, "packageName");
        String normalizedVersion = StringUtils.hasText(version) ? version.trim() : null;
        log.info("开始查询 PyPI release 文件，packageName={}，version={}，repository={}", normalizedName, normalizedVersion, repositoryBaseUrl);
        JsonNode root = fetchPackageJson(repositoryBaseUrl, normalizedName);
        JsonNode info = root.path("info");
        if (!StringUtils.hasText(normalizedVersion)) {
            normalizedVersion = textOrNull(info.path("version"));
        }
        if (!StringUtils.hasText(normalizedVersion)) {
            throw new IllegalArgumentException("PyPI 包缺少最新版本: " + normalizedName);
        }
        JsonNode filesNode = root.path("releases").path(normalizedVersion);
        if (filesNode.isMissingNode()) {
            throw new IllegalArgumentException("未找到 PyPI release: " + normalizedName + "==" + normalizedVersion);
        }
        List<PypiReleaseFile> files = new ArrayList<>();
        for (JsonNode fileNode : filesNode) {
            files.add(toReleaseFile(fileNode));
        }
        PypiReleaseFiles releaseFiles = new PypiReleaseFiles(info.path("name").asText(normalizedName), normalizedVersion,
                repositoryBaseUrl, List.copyOf(files));
        log.info("PyPI release 文件查询完成，packageName={}，version={}，fileCount={}", normalizedName, normalizedVersion, files.size());
        return releaseFiles;
    }

    private JsonNode fetchPackageJson(String repositoryBaseUrl, String normalizedName) {
        ExternalHttpResponse response = httpClient.get(buildPackageUrl(repositoryBaseUrl, normalizedName), Map.of("Accept", "application/json"));
        if (response.isNotFound()) {
            throw new IllegalArgumentException("未找到 PyPI 包: " + normalizedName);
        }
        if (!response.isSuccess()) {
            throw new ExternalServiceException("请求 PyPI JSON API 失败，status=" + response.statusCode());
        }
        return jsonSupport.readTree(response.body());
    }

    private static PypiReleaseFile toReleaseFile(JsonNode node) {
        JsonNode digests = node.path("digests");
        return new PypiReleaseFile(textOrNull(node.path("filename")), textOrNull(node.path("packagetype")),
                textOrNull(node.path("python_version")), textOrNull(node.path("url")), node.path("size").asLong(0),
                textOrNull(node.path("upload_time_iso_8601")), textOrNull(digests.path("sha256")),
                textOrNull(digests.path("md5")), textOrNull(digests.path("blake2b_256")),
                textOrNull(node.path("requires_python")), node.path("yanked").asBoolean(false),
                textOrNull(node.path("yanked_reason")));
    }

    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    static String buildPackageUrl(String repositoryBaseUrl, String packageName) {
        return repositoryBaseUrl + "/" + encode(packageName) + "/json";
    }

    private static List<String> toStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                values.add(item.asText());
            }
        }
        return List.copyOf(values);
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
