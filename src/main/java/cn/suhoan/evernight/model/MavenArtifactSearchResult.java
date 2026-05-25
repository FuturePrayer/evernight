package cn.suhoan.evernight.model;


import cn.suhoan.evernight.model.MavenArtifactSummary;
import java.util.List;

public record MavenArtifactSearchResult(long total, List<MavenArtifactSummary> artifacts) {
}
