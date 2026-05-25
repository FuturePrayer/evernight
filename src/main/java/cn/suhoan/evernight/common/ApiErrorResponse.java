package cn.suhoan.evernight.common;

import java.time.Instant;

public record ApiErrorResponse(
        String errorCode,
        String message,
        int status,
        boolean retryable,
        String path,
        Instant timestamp) {
}