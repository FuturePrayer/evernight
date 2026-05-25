package cn.suhoan.evernight.exception;

public class MavenArtifactNotFoundException extends RuntimeException {

    public MavenArtifactNotFoundException(String groupId, String artifactId, String repositoryBaseUrl) {
        super("未在仓库 %s 中找到 Maven 坐标 %s:%s".formatted(repositoryBaseUrl, groupId, artifactId));
    }

}
