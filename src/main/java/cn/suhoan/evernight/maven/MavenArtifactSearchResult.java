package cn.suhoan.evernight.maven;

import java.util.List;

public record MavenArtifactSearchResult(long total, List<MavenArtifactSummary> artifacts) {
}
