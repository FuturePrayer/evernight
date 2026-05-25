package cn.suhoan.evernight.model;

public record MavenArtifactSummary(
        String groupId,
        String artifactId,
        String latestVersion,
        String packaging,
        long timestamp,
        int versionCount) {
}
