package cn.suhoan.evernight.online;


import cn.suhoan.evernight.config.ActiveUserProperties;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActiveUserService {

    private final ActiveUserProperties properties;

    private volatile Cache<String, Boolean> activeUsers;

    private volatile long cachedWindowSeconds;

    private volatile long cachedMaximumSize;

    public ActiveUserService(ActiveUserProperties properties) {
        this.properties = properties;
    }

    public void record(String clientId) {
        if (!properties.isEnabled() || !StringUtils.hasText(clientId)) {
            return;
        }
        cache().put(clientId, Boolean.TRUE);
    }

    public long count() {
        if (!properties.isEnabled()) {
            return 0;
        }
        Cache<String, Boolean> current = cache();
        current.cleanUp();
        return current.estimatedSize();
    }

    public long windowSeconds() {
        return Math.max(1, properties.getWindowSeconds());
    }

    private Cache<String, Boolean> cache() {
        long windowSeconds = Math.max(1, properties.getWindowSeconds());
        long maximumSize = Math.max(1, properties.getMaximumSize());
        Cache<String, Boolean> current = activeUsers;
        if (current == null || cachedWindowSeconds != windowSeconds || cachedMaximumSize != maximumSize) {
            synchronized (this) {
                if (activeUsers == null || cachedWindowSeconds != windowSeconds || cachedMaximumSize != maximumSize) {
                    cachedWindowSeconds = windowSeconds;
                    cachedMaximumSize = maximumSize;
                    activeUsers = Caffeine.newBuilder()
                            .expireAfterWrite(Duration.ofSeconds(windowSeconds))
                            .maximumSize(maximumSize)
                            .build();
                }
                current = activeUsers;
            }
        }
        return current;
    }

}
