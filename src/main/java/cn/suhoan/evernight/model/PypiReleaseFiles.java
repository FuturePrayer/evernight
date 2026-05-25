package cn.suhoan.evernight.model;

import java.util.List;

public record PypiReleaseFiles(
        String name,
        String version,
        String repositoryBaseUrl,
        List<PypiReleaseFile> files) {
}
