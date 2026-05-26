package cn.suhoan.evernight.model;

public record CargoCrateSummary(
        String name,
        String latestVersion,
        String newestVersion,
        String maxStableVersion,
        String description,
        String homepage,
        String documentation,
        String repository,
        long downloads,
        long recentDownloads,
        int versionCount,
        boolean yanked) {
}
