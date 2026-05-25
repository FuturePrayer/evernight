package cn.suhoan.evernight.maven;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MavenVersionInfo(
        String groupId,
        String artifactId,
        String repositoryBaseUrl,
        String latestVersion,
        List<String> versions) {
}
