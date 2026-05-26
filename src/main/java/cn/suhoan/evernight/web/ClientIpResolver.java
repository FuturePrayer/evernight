package cn.suhoan.evernight.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public final class ClientIpResolver {

    private static final Logger log = LoggerFactory.getLogger(ClientIpResolver.class);

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request, String configuredHeaderName) {
        if (StringUtils.hasText(configuredHeaderName)) {
            String headerIp = firstIp(request.getHeader(configuredHeaderName));
            if (StringUtils.hasText(headerIp)) {
                log.info("客户端IP从配置的header中获取: {}，来源: {}", headerIp, configuredHeaderName);
                return headerIp;
            }
        }
        String forwardedFor = firstIp(request.getHeader("X-Forwarded-For"));
        if (StringUtils.hasText(forwardedFor)) {
            log.info("客户端IP从配置的header中获取: {}，来源: X-Forwarded-For", forwardedFor);
            return forwardedFor;
        }
        String realIp = firstIp(request.getHeader("X-Real-IP"));
        if (StringUtils.hasText(realIp)) {
            log.info("客户端IP从配置的header中获取: {}，来源: X-Real-IP", realIp);
            return realIp;
        }
        String remoteAddr = request.getRemoteAddr();
        log.info("客户端IP从request中获取: {}，来源: remoteAddr", remoteAddr);
        return remoteAddr;
    }

    private static String firstIp(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.split(",")[0].trim();
    }

}