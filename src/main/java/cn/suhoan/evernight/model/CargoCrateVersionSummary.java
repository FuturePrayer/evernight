package cn.suhoan.evernight.model;

public record CargoCrateVersionSummary(
        String version,
        boolean yanked,
        String license,
        String rustVersion,
        String createdAt,
        String updatedAt,
        long downloads,
        long crateSize) {
}
