package cn.suhoan.evernight.http;


import cn.suhoan.evernight.http.ExternalHttpResponse;
import java.util.Map;

public interface ExternalHttpClient {

    ExternalHttpResponse get(String url, Map<String, String> headers);

    ExternalHttpResponse postJson(String url, String body, Map<String, String> headers);

}
