package cn.suhoan.evernight.model;

import java.util.List;

public record CargoCrateInfo(
        String name,
        String description,
        String latestVersion,
        String maxStableVersion,
        String registryBaseUrl,
        String license,
        String homepage,
        String documentation,
        String repository,
        long downloads,
        long recentDownloads,
        List<String> keywords,
        List<String> categories,
        List<CargoCrateVersionSummary> versions) {
}
