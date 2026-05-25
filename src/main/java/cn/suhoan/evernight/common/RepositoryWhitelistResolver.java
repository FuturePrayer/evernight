package cn.suhoan.evernight.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

public final class RepositoryWhitelistResolver {

    private RepositoryWhitelistResolver() {
    }

    public static String resolve(String requestedBaseUrl, List<String> repositories, String repositoryType) {
        List<String> normalizedRepositories = normalizeRepositories(repositories);
        if (normalizedRepositories.isEmpty()) {
            throw new IllegalArgumentException(repositoryType + " 白名单不能为空");
        }
        String selector = StringUtils.hasText(requestedBaseUrl) ? requestedBaseUrl.trim() : normalizedRepositories.get(0);
        if (!StringUtils.hasText(selector)) {
            throw new IllegalArgumentException(repositoryType + " 默认镜像地址不能为空");
        }
        String normalizedSelector = normalizeBaseUrl(selector);
        for (String repositoryUrl : normalizedRepositories) {
            if (normalizedSelector.equals(repositoryUrl)) {
                return normalizedSelector;
            }
        }
        throw new IllegalArgumentException("不允许访问的 " + repositoryType + " 镜像地址: " + selector);
    }

    public static List<String> normalizeRepositories(List<String> repositories) {
        List<String> normalized = new ArrayList<>();
        if (repositories == null) {
            return normalized;
        }
        for (String url : repositories) {
            if (StringUtils.hasText(url)) {
                String normalizedUrl = normalizeBaseUrl(url);
                if (!normalized.contains(normalizedUrl)) {
                    normalized.add(normalizedUrl);
                }
            }
        }
        return normalized;
    }

    public static String normalizeBaseUrl(String value) {
        String normalized = InputValidator.requireText(value, "repositoryBaseUrl");
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

}
