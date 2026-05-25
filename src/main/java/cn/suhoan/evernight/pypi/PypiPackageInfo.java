package cn.suhoan.evernight.pypi;

import java.util.List;
import java.util.Map;

public record PypiPackageInfo(
        String name,
        String summary,
        String latestVersion,
        String repositoryBaseUrl,
        String requiresPython,
        String license,
        String homePage,
        List<String> classifiers,
        Map<String, String> projectUrls,
        List<String> releases) {
}
