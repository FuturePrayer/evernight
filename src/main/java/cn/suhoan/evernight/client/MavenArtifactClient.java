package cn.suhoan.evernight.client;


import cn.suhoan.evernight.client.MavenMetadataClient;
import cn.suhoan.evernight.model.MavenArtifactDetail;
import cn.suhoan.evernight.model.MavenArtifactJavaVersion;
import cn.suhoan.evernight.model.MavenArtifactSearchResult;
import cn.suhoan.evernight.model.MavenArtifactSummary;
import cn.suhoan.evernight.model.MavenDependencyDetail;
import cn.suhoan.evernight.model.MavenDependencyInfo;
import cn.suhoan.evernight.model.MavenDeveloperInfo;
import cn.suhoan.evernight.model.MavenLicenseInfo;
import cn.suhoan.evernight.model.MavenParentInfo;
import cn.suhoan.evernight.model.MavenScmInfo;
import cn.suhoan.evernight.model.MavenVersionInfo;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final String JAVA_VERSION_NOTE = "这些字段来自 POM 声明，表示“目标兼容版本”或“要求的 Java 版本”，不一定是实际执行编译的 JDK。";

    private static final int MAX_PARENT_DEPTH = 8;

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
        PomDetailWithInheritance inheritedPomDetail = inheritPomDetail(repositoryBaseUrl, pomDetail);
        log.info("Maven Artifact 详情查询完成，groupId={}，artifactId={}，version={}",
                normalizedGroupId, normalizedArtifactId, versionInfo.latestVersion());
        return new MavenArtifactDetail(normalizedGroupId, normalizedArtifactId, versionInfo.latestVersion(), packagingOrDefault(pomDetail.packaging()),
                0, versionInfo.versions().size(), pomDetail.name(), pomDetail.description(), inheritedPomDetail.url(),
                inheritedPomDetail.licenses(), inheritedPomDetail.scm(), inheritedPomDetail.developers(),
                inheritedPomDetail.inheritedFromParent());
    }

    public MavenDependencyDetail dependencyDetail(String groupId, String artifactId, String version, String repositoryBaseUrl) {
        String effectiveRepositoryBaseUrl = resolveRepositoryBaseUrl(repositoryBaseUrl);
        String cacheKey = "dependency-detail:" + effectiveRepositoryBaseUrl + ":" + groupId + ":" + artifactId + ":" + version;
        return cacheService.get("maven", cacheKey, cacheService.properties().getMavenTtlSeconds(),
                () -> doDependencyDetail(groupId, artifactId, version, effectiveRepositoryBaseUrl));
    }

    MavenDependencyDetail doDependencyDetail(String groupId, String artifactId, String version, String repositoryBaseUrl) {
        String normalizedGroupId = InputValidator.requireText(groupId, "groupId");
        String normalizedArtifactId = InputValidator.requireText(artifactId, "artifactId");
        String normalizedVersion = InputValidator.requireText(version, "version");
        log.info("开始查询 Maven 依赖详情，groupId={}，artifactId={}，version={}，repository={}",
                normalizedGroupId, normalizedArtifactId, normalizedVersion, repositoryBaseUrl);
        String pomXml = fetchPom(repositoryBaseUrl, normalizedGroupId, normalizedArtifactId, normalizedVersion);
        MavenDependencyDetail dependencyDetail = parseDependencyPom(pomXml, normalizedGroupId, normalizedArtifactId,
                normalizedVersion, repositoryBaseUrl);
        log.info("Maven 依赖详情查询完成，groupId={}，artifactId={}，version={}，dependencyCount={}，managedDependencyCount={}",
                normalizedGroupId, normalizedArtifactId, normalizedVersion, dependencyDetail.dependencies().size(),
                dependencyDetail.managedDependencies().size());
        return dependencyDetail;
    }

    public MavenArtifactJavaVersion javaVersion(String groupId, String artifactId, String version, String repositoryBaseUrl) {
        String effectiveRepositoryBaseUrl = resolveRepositoryBaseUrl(repositoryBaseUrl);
        String cacheKey = "java-version:" + effectiveRepositoryBaseUrl + ":" + groupId + ":" + artifactId + ":" + version;
        return cacheService.get("maven", cacheKey, cacheService.properties().getMavenTtlSeconds(),
                () -> doJavaVersion(groupId, artifactId, version, effectiveRepositoryBaseUrl));
    }

    MavenArtifactJavaVersion doJavaVersion(String groupId, String artifactId, String version, String repositoryBaseUrl) {
        String normalizedGroupId = InputValidator.requireText(groupId, "groupId");
        String normalizedArtifactId = InputValidator.requireText(artifactId, "artifactId");
        String normalizedVersion = StringUtils.hasText(version) ? version.trim() : null;
        if (!StringUtils.hasText(normalizedVersion)) {
            normalizedVersion = mavenMetadataClient.getLatestVersion(normalizedGroupId, normalizedArtifactId, repositoryBaseUrl)
                    .latestVersion();
        }
        log.info("开始查询 Maven Artifact Java 版本声明，groupId={}，artifactId={}，version={}，repository={}",
                normalizedGroupId, normalizedArtifactId, normalizedVersion, repositoryBaseUrl);
        String pomXml = fetchPom(repositoryBaseUrl, normalizedGroupId, normalizedArtifactId, normalizedVersion);
        MavenArtifactJavaVersion javaVersion = parseJavaVersionPom(pomXml, normalizedGroupId, normalizedArtifactId,
                normalizedVersion, repositoryBaseUrl);
        javaVersion = inheritJavaVersion(repositoryBaseUrl, javaVersion);
        log.info("Maven Artifact Java 版本声明查询完成，groupId={}，artifactId={}，version={}",
                normalizedGroupId, normalizedArtifactId, normalizedVersion);
        return javaVersion;
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
        String pomUrl = buildPomUrl(repositoryBaseUrl, groupId, artifactId, version);
        ExternalHttpResponse response = fetchPomResponse(pomUrl);
        if (!response.isSuccess()) {
            log.warn("Maven POM 查询失败，将仅返回搜索元数据，url={}，status={}", pomUrl, response.statusCode());
            return PomDetail.empty();
        }
        return parsePom(response.body());
    }

    private String fetchPom(String repositoryBaseUrl, String groupId, String artifactId, String version) {
        String pomUrl = buildPomUrl(repositoryBaseUrl, groupId, artifactId, version);
        ExternalHttpResponse response = fetchPomResponse(pomUrl);
        if (response.isNotFound()) {
            throw new IllegalArgumentException("未找到 Maven POM: " + groupId + ":" + artifactId + ":" + version);
        }
        if (!response.isSuccess()) {
            throw new ExternalServiceException("请求 Maven POM 失败，status=" + response.statusCode());
        }
        return response.body();
    }

    private ExternalHttpResponse fetchPomResponse(String pomUrl) {
        return httpClient.get(pomUrl, Map.of("Accept", "application/xml,text/xml,*/*"));
    }

    static String buildPomUrl(String repositoryBaseUrl, String groupId, String artifactId, String version) {
        return repositoryBaseUrl + "/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
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
                    readLicenses(project), readScm(project), readDevelopers(project), readParent(project), pomXml);
        }
        catch (Exception ex) {
            throw new ExternalServiceException("解析 Maven POM 失败", ex);
        }
    }

    MavenDependencyDetail parseDependencyPom(String pomXml, String groupId, String artifactId, String version,
            String repositoryBaseUrl) {
        try {
            Document document = newSecureDocumentBuilderFactory().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(pomXml)));
            Element project = document.getDocumentElement();
            return new MavenDependencyDetail(groupId, artifactId, version, repositoryBaseUrl,
                    packagingOrDefault(text(project, "packaging")), readParent(project),
                    readDependencies(directChild(project, "dependencies")),
                    readManagedDependencies(project));
        }
        catch (Exception ex) {
            throw new ExternalServiceException("解析 Maven 依赖 POM 失败", ex);
        }
    }

    MavenArtifactJavaVersion parseJavaVersionPom(String pomXml, String groupId, String artifactId, String version,
            String repositoryBaseUrl) {
        try {
            Document document = newSecureDocumentBuilderFactory().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(pomXml)));
            Element project = document.getDocumentElement();
            Map<String, String> properties = readProperties(project);
            Element compilerPlugin = findBuildPlugin(project, "org.apache.maven.plugins", "maven-compiler-plugin");
            Element compilerConfiguration = compilerPlugin == null ? null : directChild(compilerPlugin, "configuration");
            Element enforcerPlugin = findBuildPlugin(project, "org.apache.maven.plugins", "maven-enforcer-plugin");
            return new MavenArtifactJavaVersion(groupId, artifactId, version, repositoryBaseUrl,
                    properties.get("maven.compiler.release"), properties.get("maven.compiler.source"),
                    properties.get("maven.compiler.target"),
                    compilerConfiguration == null ? null : text(compilerConfiguration, "release"),
                    compilerConfiguration == null ? null : text(compilerConfiguration, "source"),
                    compilerConfiguration == null ? null : text(compilerConfiguration, "target"),
                    readEnforcerJavaVersion(enforcerPlugin), relatedJavaProperties(properties), Map.of(), JAVA_VERSION_NOTE);
        }
        catch (Exception ex) {
            throw new ExternalServiceException("解析 Maven Java 版本声明失败", ex);
        }
    }

    private static DocumentBuilderFactory newSecureDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
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

    private static MavenParentInfo readParent(Element project) {
        Element parent = directChild(project, "parent");
        if (parent == null) {
            return null;
        }
        return new MavenParentInfo(text(parent, "groupId"), text(parent, "artifactId"), text(parent, "version"),
                text(parent, "relativePath"));
    }

    private static List<MavenDependencyInfo> readManagedDependencies(Element project) {
        Element dependencyManagement = directChild(project, "dependencyManagement");
        if (dependencyManagement == null) {
            return List.of();
        }
        return readDependencies(directChild(dependencyManagement, "dependencies"));
    }

    private static List<MavenDependencyInfo> readDependencies(Element dependenciesElement) {
        if (dependenciesElement == null) {
            return List.of();
        }
        List<MavenDependencyInfo> dependencies = new ArrayList<>();
        NodeList nodes = dependenciesElement.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element dependency && "dependency".equals(dependency.getTagName())) {
                dependencies.add(new MavenDependencyInfo(text(dependency, "groupId"), text(dependency, "artifactId"),
                        text(dependency, "version"), text(dependency, "scope"), text(dependency, "type"),
                        text(dependency, "classifier"), Boolean.parseBoolean(text(dependency, "optional"))));
            }
        }
        return List.copyOf(dependencies);
    }

    private static Map<String, String> readProperties(Element project) {
        Element propertiesElement = directChild(project, "properties");
        if (propertiesElement == null) {
            return Map.of();
        }
        Map<String, String> properties = new LinkedHashMap<>();
        NodeList nodes = propertiesElement.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element property) {
                String value = property.getTextContent();
                if (StringUtils.hasText(value)) {
                    properties.put(property.getTagName(), value.trim());
                }
            }
        }
        return Map.copyOf(properties);
    }

    private static Map<String, String> relatedJavaProperties(Map<String, String> properties) {
        Map<String, String> relatedProperties = new LinkedHashMap<>();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String name = property.getKey().toLowerCase();
            if (name.contains("java") || name.contains("jdk") || name.contains("maven.compiler")) {
                relatedProperties.put(property.getKey(), property.getValue());
            }
        }
        return Map.copyOf(relatedProperties);
    }

    private static Element findBuildPlugin(Element project, String defaultGroupId, String artifactId) {
        Element build = directChild(project, "build");
        if (build == null) {
            return null;
        }
        Element plugins = directChild(build, "plugins");
        Element plugin = findPlugin(plugins, defaultGroupId, artifactId);
        if (plugin != null) {
            return plugin;
        }
        Element pluginManagement = directChild(build, "pluginManagement");
        return findPlugin(pluginManagement == null ? null : directChild(pluginManagement, "plugins"),
                defaultGroupId, artifactId);
    }

    private static Element findPlugin(Element plugins, String defaultGroupId, String artifactId) {
        if (plugins == null) {
            return null;
        }
        NodeList nodes = plugins.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element plugin && "plugin".equals(plugin.getTagName())) {
                String pluginArtifactId = text(plugin, "artifactId");
                String pluginGroupId = text(plugin, "groupId");
                if (artifactId.equals(pluginArtifactId)
                        && (!StringUtils.hasText(pluginGroupId) || defaultGroupId.equals(pluginGroupId))) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private static String readEnforcerJavaVersion(Element enforcerPlugin) {
        if (enforcerPlugin == null) {
            return null;
        }
        Element configuration = directChild(enforcerPlugin, "configuration");
        Element rules = configuration == null ? null : directChild(configuration, "rules");
        Element requireJavaVersion = rules == null ? null : directChild(rules, "requireJavaVersion");
        return requireJavaVersion == null ? null : text(requireJavaVersion, "version");
    }

    private PomDetailWithInheritance inheritPomDetail(String repositoryBaseUrl, PomDetail current) {
        String url = current.url();
        List<MavenLicenseInfo> licenses = current.licenses();
        MavenScmInfo scm = current.scm();
        List<MavenDeveloperInfo> developers = current.developers();
        Map<String, String> inheritedFromParent = new LinkedHashMap<>();
        MavenParentInfo parent = current.parent();
        Set<String> visitedParents = new HashSet<>();
        int depth = 0;
        while (parent != null && depth < MAX_PARENT_DEPTH
                && (url == null || licenses.isEmpty() || scm == null || developers.isEmpty())) {
            PomDetail parentPom = fetchParentPomDetail(repositoryBaseUrl, parent, visitedParents);
            if (parentPom == null) {
                break;
            }
            String source = parentSource(parent);
            if (url == null && StringUtils.hasText(parentPom.url())) {
                url = parentPom.url();
                inheritedFromParent.put("url", source);
            }
            if (licenses.isEmpty() && !parentPom.licenses().isEmpty()) {
                licenses = parentPom.licenses();
                inheritedFromParent.put("licenses", source);
            }
            if (scm == null && parentPom.scm() != null) {
                scm = parentPom.scm();
                inheritedFromParent.put("scm", source);
            }
            if (developers.isEmpty() && !parentPom.developers().isEmpty()) {
                developers = parentPom.developers();
                inheritedFromParent.put("developers", source);
            }
            parent = parentPom.parent();
            depth++;
        }
        return new PomDetailWithInheritance(url, licenses, scm, developers, Map.copyOf(inheritedFromParent));
    }

    private MavenArtifactJavaVersion inheritJavaVersion(String repositoryBaseUrl, MavenArtifactJavaVersion current) {
        JavaVersionValues values = JavaVersionValues.from(current);
        Map<String, String> relatedProperties = new LinkedHashMap<>(current.relatedProperties());
        Map<String, String> inheritedFromParent = new LinkedHashMap<>();
        MavenParentInfo parent = parsePom(fetchPom(repositoryBaseUrl, current.groupId(), current.artifactId(), current.version())).parent();
        Set<String> visitedParents = new HashSet<>();
        int depth = 0;
        while (parent != null && depth < MAX_PARENT_DEPTH && values.hasMissingFields()) {
            PomDetail parentPom = fetchParentPomDetail(repositoryBaseUrl, parent, visitedParents);
            if (parentPom == null) {
                break;
            }
            MavenArtifactJavaVersion parentJavaVersion = parseJavaVersionPom(parentPom.xml(), parent.groupId(),
                    parent.artifactId(), parent.version(), repositoryBaseUrl);
            values.inheritMissing(parentJavaVersion, parentSource(parent), inheritedFromParent);
            for (Map.Entry<String, String> property : parentJavaVersion.relatedProperties().entrySet()) {
                if (!relatedProperties.containsKey(property.getKey())) {
                    relatedProperties.put(property.getKey(), property.getValue());
                    inheritedFromParent.put("relatedProperties." + property.getKey(), parentSource(parent));
                }
            }
            parent = parentPom.parent();
            depth++;
        }
        return values.toResult(current, Map.copyOf(relatedProperties), Map.copyOf(inheritedFromParent));
    }

    private PomDetail fetchParentPomDetail(String repositoryBaseUrl, MavenParentInfo parent, Set<String> visitedParents) {
        String parentKey = parent.groupId() + ":" + parent.artifactId() + ":" + parent.version();
        if (!visitedParents.add(parentKey)) {
            log.warn("检测到 Maven 父 POM 循环，parent={}", parentKey);
            return null;
        }
        try {
            String parentPomXml = fetchPom(repositoryBaseUrl, parent.groupId(), parent.artifactId(), parent.version());
            return parsePom(parentPomXml);
        }
        catch (RuntimeException ex) {
            log.warn("父 POM 查询失败，将跳过继承字段，parent={}", parentKey, ex);
            return null;
        }
    }

    private static String parentSource(MavenParentInfo parent) {
        return "继承自父 POM: " + parent.groupId() + ":" + parent.artifactId() + ":" + parent.version();
    }

    private static Element directChild(Element element, String tagName) {
        NodeList nodes = element.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index) instanceof Element child && tagName.equals(child.getTagName())) {
                return child;
            }
        }
        return null;
    }

    private static String text(Element element, String tagName) {
        Element child = directChild(element, tagName);
        if (child == null) {
            return null;
        }
        String text = child.getTextContent();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    record PomDetail(String packaging, String name, String description, String url, List<MavenLicenseInfo> licenses,
            MavenScmInfo scm, List<MavenDeveloperInfo> developers, MavenParentInfo parent, String xml) {

        static PomDetail empty() {
            return new PomDetail(null, null, null, null, List.of(), null, List.of(), null, null);
        }
    }

    private record PomDetailWithInheritance(
            String url,
            List<MavenLicenseInfo> licenses,
            MavenScmInfo scm,
            List<MavenDeveloperInfo> developers,
            Map<String, String> inheritedFromParent) {
    }

    private static final class JavaVersionValues {

        private String mavenCompilerRelease;

        private String mavenCompilerSource;

        private String mavenCompilerTarget;

        private String compilerPluginRelease;

        private String compilerPluginSource;

        private String compilerPluginTarget;

        private String enforcerJavaVersion;

        private JavaVersionValues(MavenArtifactJavaVersion current) {
            this.mavenCompilerRelease = current.mavenCompilerRelease();
            this.mavenCompilerSource = current.mavenCompilerSource();
            this.mavenCompilerTarget = current.mavenCompilerTarget();
            this.compilerPluginRelease = current.compilerPluginRelease();
            this.compilerPluginSource = current.compilerPluginSource();
            this.compilerPluginTarget = current.compilerPluginTarget();
            this.enforcerJavaVersion = current.enforcerJavaVersion();
        }

        static JavaVersionValues from(MavenArtifactJavaVersion current) {
            return new JavaVersionValues(current);
        }

        boolean hasMissingFields() {
            return !StringUtils.hasText(mavenCompilerRelease)
                    || !StringUtils.hasText(mavenCompilerSource)
                    || !StringUtils.hasText(mavenCompilerTarget)
                    || !StringUtils.hasText(compilerPluginRelease)
                    || !StringUtils.hasText(compilerPluginSource)
                    || !StringUtils.hasText(compilerPluginTarget)
                    || !StringUtils.hasText(enforcerJavaVersion);
        }

        void inheritMissing(MavenArtifactJavaVersion parent, String source, Map<String, String> inheritedFromParent) {
            mavenCompilerRelease = inherit("mavenCompilerRelease", mavenCompilerRelease,
                    parent.mavenCompilerRelease(), source, inheritedFromParent);
            mavenCompilerSource = inherit("mavenCompilerSource", mavenCompilerSource,
                    parent.mavenCompilerSource(), source, inheritedFromParent);
            mavenCompilerTarget = inherit("mavenCompilerTarget", mavenCompilerTarget,
                    parent.mavenCompilerTarget(), source, inheritedFromParent);
            compilerPluginRelease = inherit("compilerPluginRelease", compilerPluginRelease,
                    parent.compilerPluginRelease(), source, inheritedFromParent);
            compilerPluginSource = inherit("compilerPluginSource", compilerPluginSource,
                    parent.compilerPluginSource(), source, inheritedFromParent);
            compilerPluginTarget = inherit("compilerPluginTarget", compilerPluginTarget,
                    parent.compilerPluginTarget(), source, inheritedFromParent);
            enforcerJavaVersion = inherit("enforcerJavaVersion", enforcerJavaVersion,
                    parent.enforcerJavaVersion(), source, inheritedFromParent);
        }

        MavenArtifactJavaVersion toResult(MavenArtifactJavaVersion current, Map<String, String> relatedProperties,
                Map<String, String> inheritedFromParent) {
            return new MavenArtifactJavaVersion(current.groupId(), current.artifactId(), current.version(),
                    current.repositoryBaseUrl(), mavenCompilerRelease, mavenCompilerSource, mavenCompilerTarget,
                    compilerPluginRelease, compilerPluginSource, compilerPluginTarget, enforcerJavaVersion,
                    relatedProperties, inheritedFromParent, current.note());
        }

        private static String inherit(String fieldName, String currentValue, String parentValue, String source,
                Map<String, String> inheritedFromParent) {
            if (StringUtils.hasText(currentValue) || !StringUtils.hasText(parentValue)) {
                return currentValue;
            }
            inheritedFromParent.put(fieldName, source);
            return parentValue;
        }

    }

}
