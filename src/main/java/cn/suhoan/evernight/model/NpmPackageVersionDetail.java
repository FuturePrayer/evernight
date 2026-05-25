package cn.suhoan.evernight.model;

import java.util.Map;

public record NpmPackageVersionDetail(
        String name,
        String version,
        String registryBaseUrl,
        String description,
        String license,
        String homepage,
        String repository,
        String deprecated,
        Map<String, String> dependencies,
        Map<String, String> devDependencies,
        Map<String, String> peerDependencies,
        Map<String, String> optionalDependencies,
        Map<String, String> engines,
        Map<String, String> bin,
        String tarball,
        String shasum,
        String integrity) {
}
