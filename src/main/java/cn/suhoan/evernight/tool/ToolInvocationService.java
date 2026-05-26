package cn.suhoan.evernight.tool;

import cn.suhoan.evernight.model.OsvPackageQuery;
import cn.suhoan.evernight.support.InputValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ToolInvocationService {

    private final CargoCrateTools cargoCrateTools;

    private final MavenRepositoryTools mavenRepositoryTools;

    private final MavenArtifactTools mavenArtifactTools;

    private final NpmPackageTools npmPackageTools;

    private final PypiPackageTools pypiPackageTools;

    private final OsvVulnerabilityTools osvVulnerabilityTools;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolInvocationService(CargoCrateTools cargoCrateTools, MavenRepositoryTools mavenRepositoryTools,
            MavenArtifactTools mavenArtifactTools, NpmPackageTools npmPackageTools, PypiPackageTools pypiPackageTools,
            OsvVulnerabilityTools osvVulnerabilityTools) {
        this.cargoCrateTools = cargoCrateTools;
        this.mavenRepositoryTools = mavenRepositoryTools;
        this.mavenArtifactTools = mavenArtifactTools;
        this.npmPackageTools = npmPackageTools;
        this.pypiPackageTools = pypiPackageTools;
        this.osvVulnerabilityTools = osvVulnerabilityTools;
    }

    public Object invoke(String tool, Map<String, Object> arguments) {
        String toolName = InputValidator.requireText(tool, "tool");
        Map<String, Object> args = arguments == null ? Map.of() : arguments;
        return switch (toolName) {
            case "maven_latest_version" -> mavenRepositoryTools.latestVersion(
                    stringArg(args, "groupId"),
                    stringArg(args, "artifactId"),
                    stringArg(args, "repositoryBaseUrl"));
            case "maven_version_list" -> mavenRepositoryTools.versionList(
                    stringArg(args, "groupId"),
                    stringArg(args, "artifactId"),
                    stringArg(args, "repositoryBaseUrl"));
            case "maven_artifact_search" -> mavenArtifactTools.search(
                    stringArg(args, "keyword"),
                    stringArg(args, "groupId"),
                    stringArg(args, "artifactId"),
                    integerArg(args, "rows"),
                    stringArg(args, "repositoryBaseUrl"));
            case "maven_artifact_detail" -> mavenArtifactTools.detail(
                    stringArg(args, "groupId"),
                    stringArg(args, "artifactId"),
                    stringArg(args, "repositoryBaseUrl"));
            case "maven_dependency_detail" -> mavenArtifactTools.dependencyDetail(
                    stringArg(args, "groupId"),
                    stringArg(args, "artifactId"),
                    stringArg(args, "version"),
                    stringArg(args, "repositoryBaseUrl"));
            case "maven_artifact_java_version" -> mavenArtifactTools.javaVersion(
                    stringArg(args, "groupId"),
                    stringArg(args, "artifactId"),
                    stringArg(args, "version"),
                    stringArg(args, "repositoryBaseUrl"));
            case "npm_package_info" -> npmPackageTools.packageInfo(
                    stringArg(args, "packageName"),
                    integerArg(args, "versionLimit"),
                    stringArg(args, "registryBaseUrl"));
            case "npm_package_version_detail" -> npmPackageTools.versionDetail(
                    stringArg(args, "packageName"),
                    stringArg(args, "version"),
                    stringArg(args, "registryBaseUrl"));
            case "pypi_package_info" -> pypiPackageTools.packageInfo(
                    stringArg(args, "packageName"),
                    integerArg(args, "releaseLimit"),
                    stringArg(args, "repositoryBaseUrl"));
            case "pypi_release_files" -> pypiPackageTools.releaseFiles(
                    stringArg(args, "packageName"),
                    stringArg(args, "version"),
                    stringArg(args, "repositoryBaseUrl"));
            case "cargo_crate_search" -> cargoCrateTools.search(
                    stringArg(args, "keyword"),
                    integerArg(args, "perPage"),
                    stringArg(args, "registryBaseUrl"));
            case "cargo_crate_info" -> cargoCrateTools.crateInfo(
                    stringArg(args, "crateName"),
                    integerArg(args, "versionLimit"),
                    stringArg(args, "registryBaseUrl"));
            case "cargo_crate_version_detail" -> cargoCrateTools.versionDetail(
                    stringArg(args, "crateName"),
                    stringArg(args, "version"),
                    stringArg(args, "registryBaseUrl"));
            case "osv_vulnerability_lookup" -> osvVulnerabilityTools.lookup(
                    stringArg(args, "ecosystem"),
                    stringArg(args, "packageName"),
                    stringArg(args, "version"));
            case "osv_batch_vulnerability_lookup" -> osvVulnerabilityTools.batchLookup(packagesArg(args, "packages"));
            default -> throw new IllegalArgumentException("未知工具: " + toolName);
        };
    }

    private static String stringArg(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private static Integer integerArg(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = value.toString();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        }
        catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " 必须是整数");
        }
    }

    private List<OsvPackageQuery> packagesArg(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (value == null) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        if (value instanceof List<?> list) {
            List<OsvPackageQuery> queries = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    queries.add(new OsvPackageQuery(asString(map.get("ecosystem")), asString(map.get("packageName")),
                            asString(map.get("version"))));
                }
            }
            return List.copyOf(queries);
        }
        String text = value.toString();
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        try {
            return objectMapper.readValue(text, new TypeReference<List<OsvPackageQuery>>() {
            });
        }
        catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(name + " 必须是 JSON 数组", ex);
        }
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

}
