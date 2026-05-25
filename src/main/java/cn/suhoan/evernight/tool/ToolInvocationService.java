package cn.suhoan.evernight.tool;

import cn.suhoan.evernight.support.InputValidator;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ToolInvocationService {

    private final MavenRepositoryTools mavenRepositoryTools;

    private final MavenArtifactTools mavenArtifactTools;

    private final NpmPackageTools npmPackageTools;

    private final PypiPackageTools pypiPackageTools;

    private final OsvVulnerabilityTools osvVulnerabilityTools;

    public ToolInvocationService(MavenRepositoryTools mavenRepositoryTools, MavenArtifactTools mavenArtifactTools,
            NpmPackageTools npmPackageTools, PypiPackageTools pypiPackageTools,
            OsvVulnerabilityTools osvVulnerabilityTools) {
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
            case "npm_package_info" -> npmPackageTools.packageInfo(
                    stringArg(args, "packageName"),
                    integerArg(args, "versionLimit"),
                    stringArg(args, "registryBaseUrl"));
            case "pypi_package_info" -> pypiPackageTools.packageInfo(
                    stringArg(args, "packageName"),
                    integerArg(args, "releaseLimit"),
                    stringArg(args, "repositoryBaseUrl"));
            case "osv_vulnerability_lookup" -> osvVulnerabilityTools.lookup(
                    stringArg(args, "ecosystem"),
                    stringArg(args, "packageName"),
                    stringArg(args, "version"));
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

}
