package cn.suhoan.evernight.common;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@EnableConfigurationProperties(RateLimitProperties.class)
public class IpRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpRateLimitFilter.class);

    private final RateLimitProperties properties;

    private volatile Cache<String, AtomicInteger> counters;

    private volatile long cachedWindowSeconds;

    public IpRateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String clientIp = ClientIpResolver.resolve(request, properties.getClientIpHeader());
        int count = cache().get(clientIp, ignored -> new AtomicInteger()).incrementAndGet();
        if (count > properties.getMaxRequests()) {
            log.warn("客户端 IP 触发限流，clientIp={}, count={}, maxRequests={}", clientIp, count, properties.getMaxRequests());
            throw new RateLimitExceededException("请求过于频繁，请稍后再试");
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(properties.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, properties.getMaxRequests() - count)));
        filterChain.doFilter(request, response);
    }

    private Cache<String, AtomicInteger> cache() {
        long windowSeconds = Math.max(1, properties.getWindowSeconds());
        Cache<String, AtomicInteger> current = counters;
        if (current == null || cachedWindowSeconds != windowSeconds) {
            synchronized (this) {
                if (counters == null || cachedWindowSeconds != windowSeconds) {
                    cachedWindowSeconds = windowSeconds;
                    counters = Caffeine.newBuilder()
                            .expireAfterWrite(Duration.ofSeconds(windowSeconds))
                            .maximumSize(100000)
                            .build();
                }
                current = counters;
            }
        }
        return current;
    }

}