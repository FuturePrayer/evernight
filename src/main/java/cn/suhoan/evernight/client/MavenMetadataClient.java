package cn.suhoan.evernight.client;


import cn.suhoan.evernight.config.MavenRepositoryProperties;
import cn.suhoan.evernight.exception.MavenArtifactNotFoundException;
import cn.suhoan.evernight.model.MavenVersionInfo;
import cn.suhoan.evernight.whitelist.MavenRepositoryResolver;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cn.suhoan.evernight.http.ExternalHttpClient;
import cn.suhoan.evernight.http.ExternalHttpResponse;
import cn.suhoan.evernight.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Service
@EnableConfigurationProperties(MavenRepositoryProperties.class)
public class MavenMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(MavenMetadataClient.class);

    private final MavenRepositoryResolver repositoryResolver;

    private final ExternalHttpClient httpClient;

    public MavenMetadataClient(MavenRepositoryResolver repositoryResolver, ExternalHttpClient httpClient) {
        this.repositoryResolver = repositoryResolver;
        this.httpClient = httpClient;
    }

    public MavenVersionInfo getVersions(String groupId, String artifactId, String repositoryBaseUrl) {
        String normalizedGroupId = requireText(groupId, "groupId");
        String normalizedArtifactId = requireText(artifactId, "artifactId");
        String effectiveRepositoryBaseUrl = resolveRepositoryBaseUrl(repositoryBaseUrl);
        URI metadataUri = buildMetadataUri(effectiveRepositoryBaseUrl, normalizedGroupId, normalizedArtifactId);

        log.info("开始查询 Maven 元数据，groupId={}, artifactId={}, repository={}",
                normalizedGroupId, normalizedArtifactId, effectiveRepositoryBaseUrl);

        String metadataXml = fetchMetadata(metadataUri, normalizedGroupId, normalizedArtifactId, effectiveRepositoryBaseUrl);
        MavenVersionInfo versionInfo = parseMetadata(metadataXml, normalizedGroupId, normalizedArtifactId,
                effectiveRepositoryBaseUrl);

        log.info("Maven 元数据查询完成，groupId={}, artifactId={}, latestVersion={}, versionCount={}",
                normalizedGroupId, normalizedArtifactId, versionInfo.latestVersion(), versionInfo.versions().size());
        return versionInfo;
    }

    public MavenVersionInfo getLatestVersion(String groupId, String artifactId, String repositoryBaseUrl) {
        MavenVersionInfo versionInfo = getVersions(groupId, artifactId, repositoryBaseUrl);
        return new MavenVersionInfo(versionInfo.groupId(), versionInfo.artifactId(), versionInfo.repositoryBaseUrl(),
                versionInfo.latestVersion(), List.of());
    }

    String resolveRepositoryBaseUrl(String repositoryBaseUrl) {
        return repositoryResolver.resolve(repositoryBaseUrl);
    }

    URI buildMetadataUri(String repositoryBaseUrl, String groupId, String artifactId) {
        String groupPath = groupId.replace('.', '/');
        return URI.create("%s/%s/%s/maven-metadata.xml".formatted(
                trimTrailingSlash(repositoryBaseUrl), groupPath, artifactId));
    }

    MavenVersionInfo parseMetadata(String metadataXml, String groupId, String artifactId, String repositoryBaseUrl) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用远程 XML 的外部实体，避免 XXE 风险。
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(metadataXml)));
            Element versioning = firstElement(document, "versioning");
            if (versioning == null) {
                throw new IllegalStateException("Maven 元数据缺少 versioning 节点");
            }

            List<String> versions = readVersions(versioning);
            if (versions.isEmpty()) {
                throw new MavenArtifactNotFoundException(groupId, artifactId, repositoryBaseUrl);
            }

            String latestVersion = firstText(versioning, "latest");
            if (!StringUtils.hasText(latestVersion)) {
                latestVersion = firstText(versioning, "release");
            }
            if (!StringUtils.hasText(latestVersion)) {
                latestVersion = versions.get(versions.size() - 1);
            }

            return new MavenVersionInfo(groupId, artifactId, repositoryBaseUrl, latestVersion, List.copyOf(versions));
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IllegalStateException("解析 Maven 元数据失败", ex);
        }
    }

    private String fetchMetadata(URI metadataUri, String groupId, String artifactId, String repositoryBaseUrl) {
        ExternalHttpResponse response = httpClient.get(metadataUri.toString(), Map.of("Accept", "application/xml,text/xml,*/*"));
        if (response.isSuccess()) {
            return response.body();
        }
        if (response.isNotFound() || (response.statusCode() >= 400 && response.statusCode() < 500)) {
            log.warn("未找到 Maven 元数据，groupId={}, artifactId={}, repository={}", groupId, artifactId, repositoryBaseUrl);
            throw new MavenArtifactNotFoundException(groupId, artifactId, repositoryBaseUrl);
        }
        log.error("请求 Maven 仓库失败，uri={}, status={}", metadataUri, response.statusCode());
        throw new ExternalServiceException("请求 Maven 仓库失败: " + metadataUri);
    }

    private static String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value.trim();
    }

    private static String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static Element firstElement(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return null;
        }
        return (Element) nodeList.item(0);
    }

    private static String firstText(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return null;
        }
        return nodeList.item(0).getTextContent();
    }

    private static List<String> readVersions(Element versioning) {
        NodeList versionNodes = versioning.getElementsByTagName("version");
        List<String> versions = new ArrayList<>();
        for (int index = 0; index < versionNodes.getLength(); index++) {
            String version = versionNodes.item(index).getTextContent();
            if (StringUtils.hasText(version)) {
                versions.add(version.trim());
            }
        }
        return versions;
    }

}
