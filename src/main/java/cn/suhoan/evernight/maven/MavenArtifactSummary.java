package cn.suhoan.evernight.maven;

public record MavenArtifactSummary(
        String groupId,
        String artifactId,
        String latestVersion,
        String packaging,
        long timestamp,
        int versionCount) {
}
