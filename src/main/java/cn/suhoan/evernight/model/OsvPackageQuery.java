package cn.suhoan.evernight.model;

public record OsvPackageQuery(
        String ecosystem,
        String packageName,
        String version) {
}
