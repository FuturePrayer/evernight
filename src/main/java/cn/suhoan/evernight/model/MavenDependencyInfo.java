package cn.suhoan.evernight.model;

public record MavenDependencyInfo(
        String groupId,
        String artifactId,
        String version,
        String scope,
        String type,
        String classifier,
        boolean optional) {
}
