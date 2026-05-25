package cn.suhoan.evernight.pypi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import cn.suhoan.evernight.common.EvernightCacheService;
import cn.suhoan.evernight.common.ExternalHttpClient;
import cn.suhoan.evernight.common.ExternalHttpResponse;
import cn.suhoan.evernight.common.ExternalServiceException;
import cn.suhoan.evernight.common.InputValidator;
import cn.suhoan.evernight.common.JsonSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

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

    PypiPackageInfo doPackageInfo(String packageName, Integer releaseLimit, String repositoryBaseUrl) {
        String normalizedName = InputValidator.requireText(packageName, "packageName");
        int limit = InputValidator.clampPageSize(releaseLimit, 20, 100);
        log.info("开始查询 PyPI 包信息，packageName={}，releaseLimit={}，repository={}", normalizedName, limit, repositoryBaseUrl);
        ExternalHttpResponse response = httpClient.get(buildPackageUrl(repositoryBaseUrl, normalizedName), Map.of("Accept", "application/json"));
        if (response.isNotFound()) {
            throw new IllegalArgumentException("未找到 PyPI 包: " + normalizedName);
        }
        if (!response.isSuccess()) {
            throw new ExternalServiceException("请求 PyPI JSON API 失败，status=" + response.statusCode());
        }
        JsonNode root = jsonSupport.readTree(response.body());
        JsonNode info = root.path("info");
        PypiPackageInfo packageInfo = new PypiPackageInfo(info.path("name").asText(normalizedName), textOrNull(info.path("summary")),
                textOrNull(info.path("version")), repositoryBaseUrl, textOrNull(info.path("requires_python")),
                textOrNull(info.path("license")), textOrNull(info.path("home_page")), toStringList(info.path("classifiers")),
                toStringMap(info.path("project_urls")), limitedFieldNames(root.path("releases"), limit));
        log.info("PyPI 包信息查询完成，packageName={}，latestVersion={}，releaseCount={}", normalizedName, packageInfo.latestVersion(), packageInfo.releases().size());
        return packageInfo;
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
