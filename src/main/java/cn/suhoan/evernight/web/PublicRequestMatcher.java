package cn.suhoan.evernight.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class PublicRequestMatcher {

    private PublicRequestMatcher() {
    }

    public static boolean isHomePageRequest(HttpServletRequest request) {
        String path = pathWithinApplication(request);
        return path.equals("/")
                || path.equals("/index.html")
                || path.equals("/tools.html")
                || path.equals("/api/repositories")
                || path.equals("/api/online-users")
                || path.equals("/api/human-challenge")
                || path.equals("/actuator")
                || path.startsWith("/actuator/")
                || isStaticAsset(path);
    }

    private static boolean isStaticAsset(String path) {
        return path.equals("/app.js")
                || path.equals("/tools.js")
                || path.equals("/styles.css")
                || path.equals("/icon.svg")
                || path.equals("/favicon.ico");
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }
        return StringUtils.hasText(requestUri) ? requestUri : "/";
    }

}
