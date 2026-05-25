package cn.suhoan.evernight.support;


import cn.suhoan.evernight.cache.EvernightCacheService;
import cn.suhoan.evernight.config.CacheProperties;
public final class TestSupport {

    private TestSupport() {
    }

    public static EvernightCacheService disabledCacheService() {
        CacheProperties properties = new CacheProperties();
        properties.setEnabled(false);
        return new EvernightCacheService(properties);
    }

}