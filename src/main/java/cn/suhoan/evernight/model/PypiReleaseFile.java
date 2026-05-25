package cn.suhoan.evernight.model;

public record PypiReleaseFile(
        String filename,
        String packageType,
        String pythonVersion,
        String url,
        long size,
        String uploadTime,
        String sha256Digest,
        String md5Digest,
        String blake2bDigest,
        String requiresPython,
        boolean yanked,
        String yankedReason) {
}
