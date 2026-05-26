package cn.suhoan.evernight.model;

import java.util.List;
import java.util.Map;

public record CargoCrateVersionDetail(
        String name,
        String version,
        String registryBaseUrl,
        String description,
        String license,
        String homepage,
        String documentation,
        String repository,
        String rustVersion,
        String edition,
        String checksum,
        boolean yanked,
        String yankMessage,
        long crateSize,
        long downloads,
        String createdAt,
        String updatedAt,
        Map<String, List<String>> features,
        List<CargoDependencyInfo> dependencies,
        String note) {
}
