package cn.suhoan.evernight.online;


import cn.suhoan.evernight.config.ActiveUserProperties;
import cn.suhoan.evernight.config.RateLimitProperties;
import cn.suhoan.evernight.online.ActiveUserService;
import cn.suhoan.evernight.web.ClientIpResolver;
import cn.suhoan.evernight.web.PublicRequestMatcher;
import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@EnableConfigurationProperties({ ActiveUserProperties.class, RateLimitProperties.class })
public class ActiveUserTrackingFilter extends OncePerRequestFilter {

    private final ActiveUserService activeUserService;

    private final ActiveUserProperties activeUserProperties;

    private final RateLimitProperties rateLimitProperties;

    public ActiveUserTrackingFilter(ActiveUserService activeUserService, ActiveUserProperties activeUserProperties,
            RateLimitProperties rateLimitProperties) {
        this.activeUserService = activeUserService;
        this.activeUserProperties = activeUserProperties;
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PublicRequestMatcher.isHomePageRequest(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (activeUserProperties.isEnabled()) {
            activeUserService.record(ClientIpResolver.resolve(request, rateLimitProperties.getClientIpHeader()));
        }
        filterChain.doFilter(request, response);
    }

}
