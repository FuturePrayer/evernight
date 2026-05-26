package cn.suhoan.evernight.model;

import java.util.List;

public record CargoDependencyInfo(
        String crateName,
        String requirement,
        String kind,
        boolean optional,
        boolean defaultFeatures,
        List<String> features,
        String target,
        long downloads) {
}
