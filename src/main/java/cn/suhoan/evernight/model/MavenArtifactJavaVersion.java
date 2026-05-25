package cn.suhoan.evernight.model;

import java.util.Map;

public record MavenArtifactJavaVersion(
        String groupId,
        String artifactId,
        String version,
        String repositoryBaseUrl,
        String mavenCompilerRelease,
        String mavenCompilerSource,
        String mavenCompilerTarget,
        String compilerPluginRelease,
        String compilerPluginSource,
        String compilerPluginTarget,
        String enforcerJavaVersion,
        Map<String, String> relatedProperties,
        Map<String, String> inheritedFromParent,
        String note) {
}
