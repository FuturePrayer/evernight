package cn.suhoan.evernight.maven;

import java.util.List;

public record MavenArtifactDetail(
        String groupId,
        String artifactId,
        String latestVersion,
        String packaging,
        long timestamp,
        int versionCount,
        String name,
        String description,
        String url,
        List<MavenLicenseInfo> licenses,
        MavenScmInfo scm,
        List<MavenDeveloperInfo> developers) {
}
