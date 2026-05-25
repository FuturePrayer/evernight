package cn.suhoan.evernight.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request, String configuredHeaderName) {
        if (StringUtils.hasText(configuredHeaderName)) {
            String headerIp = firstIp(request.getHeader(configuredHeaderName));
            if (StringUtils.hasText(headerIp)) {
                return headerIp;
            }
        }
        String forwardedFor = firstIp(request.getHeader("X-Forwarded-For"));
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor;
        }
        String realIp = firstIp(request.getHeader("X-Real-IP"));
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private static String firstIp(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.split(",")[0].trim();
    }

}