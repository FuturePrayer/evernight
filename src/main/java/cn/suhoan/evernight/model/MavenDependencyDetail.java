package cn.suhoan.evernight.model;

import java.util.List;

public record MavenDependencyDetail(
        String groupId,
        String artifactId,
        String version,
        String repositoryBaseUrl,
        String packaging,
        MavenParentInfo parent,
        List<MavenDependencyInfo> dependencies,
        List<MavenDependencyInfo> managedDependencies) {
}
