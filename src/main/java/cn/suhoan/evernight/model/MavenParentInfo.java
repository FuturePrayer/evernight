package cn.suhoan.evernight.model;

public record MavenParentInfo(
        String groupId,
        String artifactId,
        String version,
        String relativePath) {
}
