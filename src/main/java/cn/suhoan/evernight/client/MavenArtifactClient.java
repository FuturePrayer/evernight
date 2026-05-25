package cn.suhoan.evernight.client;


import cn.suhoan.evernight.client.MavenMetadataClient;
import cn.suhoan.evernight.model.MavenArtifactDetail;
import cn.suhoan.evernight.model.MavenArtifactSearchResult;
import cn.suhoan.evernight.model.MavenArtifactSummary;
import cn.suhoan.evernight.model.MavenDeveloperInfo;
import cn.suhoan.evernight.model.MavenLicenseInfo;
import cn.suhoan.evernight.model.MavenScmInfo;
import cn.suhoan.evernight.model.MavenVersionInfo;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import cn.suhoan.evernight.cache.EvernightCacheService;
import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.exception.ExternalServiceException;
import cn.suhoan.evernight.support.InputValidator;
import cn.suhoan.evernight.support.JsonSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class MavenArtifactClient {

    private static final Logger log = LoggerFactory.getLogger(MavenArtifactClient.class);

    private static final String SEARCH_URL = "https://search.maven.org/solrsearch/select";

    private static final String CENTRAL_REPOSITORY_URL = "https://repo1.maven.org/maven2";

    private final ExternalHttpClient httpClient;

    private final JsonSupport jsonSupport;

    private final EvernightCacheService cacheService;

    private final MavenRepositoryResolver repositoryResolver;

    private final MavenMetadataClient mavenMetadataClient;

    @Autowired
    public MavenArtifactClient(ExternalHttpClient httpClient, JsonSupport jsonSupport, EvernightCacheService cacheService,
            MavenRepositoryResolver repositoryResolver, MavenMetadataClient mavenMetadataClient) {
        this.httpClient = httpClient;
        this.jsonSupport = jsonSupport;
        this.cacheService = cacheService;
        this.repositoryResolver = repositoryResolver;
        this.mavenMetadataClient = mavenMetadataClient;
    }

    MavenArtifactClient(ExternalHttpClient httpClient, JsonSupport jsonSupport, EvernightCacheService cacheService) {
        this(httpClient, jsonSupport, cacheService, null, null);
    }

    public MavenArtifactSearchResult search(String keyword, String groupId, String artifactId, Integer rows) {
        return search(keyword, groupId, artifactId, rows, null);
    }

    public MavenArtifactSearchResult search(String keyword, String groupId, String artifactId, Integer rows, String repositoryBaseUrl) {
        String effectiveRepositoryBaseUrl = resolveRepositoryBaseUrl(repositoryBaseUrl);
        String cacheKey = "search:" + effectiveRepositoryBaseUrl + ":" + keyword + ":" + groupId + ":" + artifactId + ":" + rows;
        return cacheService.get("maven", cacheKey, cacheService.properties().getMavenTtlSeconds(),
                () -> doSearch(keyword, groupId, artifactId, rows, effectiveRepositoryBaseUrl));
    }

    MavenArtifactSearchResult doSearch(String keyword, String groupId, String artifactId, Integer rows, String repositoryBaseUrl) {
        if (!CENTRAL_REPOSITORY_URL.equals(repositoryBaseUrl)) {
            return doRepositorySearch(keyword, groupId, artifactId, repositoryBaseUrl);
        }
        int pageSize = InputValidator.clampPageSize(rows, 10, 50);
        String query = buildSearchQuery(keyword, groupId, artifactId);
        String url = SEARCH_URL + "?wt=json&rows=" + pageSize + "&q=" + encode(query);
        log.info("开始搜索 Maven Artifact，query={}，rows={}", query, pageSize);
        JsonNode root = getJson(url);
        JsonNode response = root.path("response");
        List<MavenArtifactSummary> artifacts = new ArrayList<>();
        for (JsonNode doc : response.path("docs")) {
            artifacts.add(toSummary(doc));
        }
        log.info("Maven Artifact 搜索完成，query={}，total={}，returned={}", query, response.path("numFound").asLong(), artifacts.size());
        return new MavenArtifactSearchResult(response.path("numFound").asLong(), List.copyOf(artifacts));
    }

    private MavenArtifactSearchResult doRepositorySearch(String keyword, String groupId, String artifactId, String repositoryBaseUrl) {
        if (StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("Maven 仓库镜像不支持关键词搜索，请使用 groupId 和 artifactId 精确查询");
        }
        String normalizedGroupId = InputValidator.requireText(groupId, "groupId");
        String normalizedArtifactId = InputValidator.requireText(artifactId, "artifactId");
        MavenVersionInfo versionInfo = mavenMetadataClient.getVersions(normalizedGroupId, normalizedArtifactId, repositoryBaseUrl);
        MavenArtifactSummary summary = new MavenArtifactSummary(normalizedGroupId, normalizedArtifactId,
                versionInfo.latestVersion(), null, 0, versionInfo.versions().size());
        return new MavenArtifactSearchResult(1, List.of(summary));
    }

    public MavenArtifactDetail detail(String groupId, String artifactId) {
        return detail(groupId, artifactId, null);
    }

    public MavenArtifactDetail detail(String groupId, String artifactId, String repositoryBaseUrl) {
        String effectiveRepositoryBaseUrl = resolveRepositoryBaseUrl(repositoryBaseUrl);
        String cacheKey = "detail:" + effectiveRepositoryBaseUrl + ":" + groupId + ":" + artifactId;
        return cacheService.get("maven", cacheKey, cacheService.properties().getMavenTtlSeconds(),
                () -> doDetail(groupId, artifactId, effectiveRepositoryBaseUrl));
    }

    MavenArtifactDetail doDetail(String groupId, String artifactId, String repositoryBaseUrl) {
        String normalizedGroupId = InputValidator.requireText(groupId, "groupId");
        String normalizedArtifactId = InputValidator.requireText(artifactId, "artifactId");
        log.info("开始查询 Maven Artifact 详情，groupId={}，artifactId={}，repository={}",
                normalizedGroupId, normalizedArtifactId, repositoryBaseUrl);
        MavenVersionInfo versionInfo = mavenMetadataClient.getVersions(normalizedGroupId, normalizedArtifactId, repositoryBaseUrl);
        PomDetail pomDetail = fetchPomDetail(repositoryBaseUrl, normalizedGroupId, normalizedArtifactId, versionInfo.latestVersion());
        log.info("Maven Artifact 详情查询完成，groupId={}，artifactId={}，version={}",
                normalizedGroupId, normalizedArtifactId, versionInfo.latestVersion());
        return new MavenArtifactDetail(normalizedGroupId, normalizedArtifactId, versionInfo.latestVersion(), packagingOrDefault(pomDetail.packaging()),
                0, versionInfo.versions().size(), pomDetail.name(), pomDetail.description(), pomDetail.url(),
                pomDetail.licenses(), pomDetail.scm(), pomDetail.developers());
    }

    private String resolveRepositoryBaseUrl(String repositoryBaseUrl) {
        if (repositoryResolver == null) {
            return CENTRAL_REPOSITORY_URL;
        }
        return repositoryResolver.resolve(repositoryBaseUrl);
    }

    private JsonNode getJson(String url) {
        ExternalHttpResponse response = httpClient.get(url, Map.of("Accept", "application/json"));
        if (response.isSuccess()) {
            return jsonSupport.readTree(response.body());
        }
        throw new ExternalServiceException("请求 Maven Central Search API 失败，status=" + response.statusCode());
    }

    private PomDetail fetchPomDetail(String repositoryBaseUrl, String groupId, String artifactId, String version) {
        String pomUrl = repositoryBaseUrl + "/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
        ExternalHttpResponse response = httpClient.get(pomUrl, Map.of("Accept", "application/xml,text/xml,*/*"));
        if (!response.isSuccess()) {
            log.warn("Maven POM 查询失败，将仅返回搜索元数据，url={}，status={}", pomUrl, response.statusCode());
            return PomDetail.empty();
        }
        return parsePom(response.body());
    }

    PomDetail parsePom(String pomXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Remote POM is untrusted input, so external entities must be disabled.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(pomXml)));
            Element project = document.getDocumentElement();
            return new PomDetail(text(project, "packaging"), text(project, "name"), text(project, "description"), text(project, "url"),
                    readLicenses(project), readScm(project), readDevelopers(project));
        }
        catch (Exception ex) {
            throw new ExternalServiceException("解析 Maven POM 失败", ex);
        }
    }

    static String buildSearchQuery(String keyword, String groupId, String artifactId) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            parts.add(keyword.trim());
        }
        if (StringUtils.hasText(groupId)) {
            parts.add("g:\"" + groupId.trim() + "\"");
        }
        if (StringUtils.hasText(artifactId)) {
            parts.add("a:\"" + artifactId.trim() + "\"");
        }
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("keyword、groupId、artifactId 至少需要提供一个");
        }
        return String.join(" AND ", parts);
    }

    private static MavenArtifactSummary toSummary(JsonNode doc) {
        return new MavenArtifactSummary(doc.path("g").asText(), doc.path("a").asText(), doc.path("latestVersion").asText(),
                doc.path("p").asText(null), doc.path("timestamp").asLong(), doc.path("versionCount").asInt());
    }

    private static String packagingOrDefault(String packaging) {
        return StringUtils.hasText(packaging) ? packaging : "jar";
    }

    private static List<MavenLicenseInfo> readLicenses(Element project) {
        List<MavenLicenseInfo> licenses = new ArrayList<>();
        NodeList nodes = project.getElementsByTagName("license");
        for (int index = 0; index < nodes.getLength(); index++) {
            Element license = (Element) nodes.item(index);
            licenses.add(new MavenLicenseInfo(text(license, "name"), text(license, "url")));
        }
        return List.copyOf(licenses);
    }

    private static MavenScmInfo readScm(Element project) {
        NodeList nodes = project.getElementsByTagName("scm");
        if (nodes.getLength() == 0) {
            return null;
        }
        Element scm = (Element) nodes.item(0);
        return new MavenScmInfo(text(scm, "url"), text(scm, "connection"), text(scm, "developerConnection"));
    }

    private static List<MavenDeveloperInfo> readDevelopers(Element project) {
        List<MavenDeveloperInfo> developers = new ArrayList<>();
        NodeList nodes = project.getElementsByTagName("developer");
        for (int index = 0; index < nodes.getLength(); index++) {
            Element developer = (Element) nodes.item(index);
            developers.add(new MavenDeveloperInfo(text(developer, "id"), text(developer, "name"), text(developer, "email")));
        }
        return List.copyOf(developers);
    }

    private static String text(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        String text = nodes.item(0).getTextContent();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    record PomDetail(String packaging, String name, String description, String url, List<MavenLicenseInfo> licenses,
            MavenScmInfo scm, List<MavenDeveloperInfo> developers) {

        static PomDetail empty() {
            return new PomDetail(null, null, null, null, List.of(), null, List.of());
        }
    }

}
