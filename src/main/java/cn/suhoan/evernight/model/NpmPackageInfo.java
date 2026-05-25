package cn.suhoan.evernight.model;

import java.util.List;
import java.util.Map;

public record NpmPackageInfo(
        String name,
        String description,
        String latestVersion,
        String registryBaseUrl,
        Map<String, String> distTags,
        String license,
        String homepage,
        String repository,
        Map<String, String> dependencies,
        List<String> versions) {
}
