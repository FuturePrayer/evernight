package cn.suhoan.evernight.http;

public record ExternalHttpResponse(int statusCode, String body) {

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

}
