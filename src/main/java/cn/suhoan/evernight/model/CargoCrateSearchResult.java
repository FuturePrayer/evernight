package cn.suhoan.evernight.model;

import java.util.List;

public record CargoCrateSearchResult(
        long total,
        String registryBaseUrl,
        List<CargoCrateSummary> crates) {
}
