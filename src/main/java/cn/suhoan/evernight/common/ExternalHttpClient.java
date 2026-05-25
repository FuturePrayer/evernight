package cn.suhoan.evernight.common;

import java.util.Map;

public interface ExternalHttpClient {

    ExternalHttpResponse get(String url, Map<String, String> headers);

    ExternalHttpResponse postJson(String url, String body, Map<String, String> headers);

}
