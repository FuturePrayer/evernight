package cn.suhoan.evernight.common;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(CacheProperties.class)
public class EvernightCacheService {

    private final CacheProperties properties;

    private final Map<String, Cache<String, Object>> caches = new ConcurrentHashMap<>();

    public EvernightCacheService(CacheProperties properties) {
        this.properties = properties;
    }

    public <T> T get(String cacheName, String key, long ttlSeconds, Supplier<T> supplier) {
        if (!properties.isEnabled()) {
            return supplier.get();
        }
        Cache<String, Object> cache = caches.computeIfAbsent(cacheName, ignored -> Caffeine.newBuilder()
                .maximumSize(properties.getMaximumSize())
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .build());
        @SuppressWarnings("unchecked")
        T value = (T) cache.get(key, ignored -> supplier.get());
        return value;
    }

    public CacheProperties properties() {
        return properties;
    }

}