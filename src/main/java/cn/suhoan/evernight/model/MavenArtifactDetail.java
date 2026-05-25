package cn.suhoan.evernight.model;


import cn.suhoan.evernight.model.MavenDeveloperInfo;
import cn.suhoan.evernight.model.MavenLicenseInfo;
import cn.suhoan.evernight.model.MavenScmInfo;
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
