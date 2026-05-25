package cn.suhoan.evernight.common;

public final class TestSupport {

    private TestSupport() {
    }

    public static EvernightCacheService disabledCacheService() {
        CacheProperties properties = new CacheProperties();
        properties.setEnabled(false);
        return new EvernightCacheService(properties);
    }

}