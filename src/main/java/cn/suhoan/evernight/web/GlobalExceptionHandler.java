package cn.suhoan.evernight.web;


import cn.suhoan.evernight.exception.ExternalServiceException;
import cn.suhoan.evernight.exception.RateLimitExceededException;
import cn.suhoan.evernight.web.ApiErrorResponse;
import java.time.Instant;

import cn.suhoan.evernight.exception.MavenArtifactNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        return error(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage(), true, request);
    }

    @ExceptionHandler(MavenArtifactNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleMavenNotFound(MavenArtifactNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND", ex.getMessage(), false, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), false, request);
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiErrorResponse> handleExternalService(ExternalServiceException ex, HttpServletRequest request) {
        log.warn("外部服务调用失败，path={}", request.getRequestURI(), ex);
        return error(HttpStatus.BAD_GATEWAY, "UPSTREAM_ERROR", ex.getMessage(), true, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("服务处理请求失败，path={}", request.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "服务内部错误", true, request);
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String errorCode, String message,
            boolean retryable, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(errorCode, message, status.value(), retryable, request.getRequestURI(), Instant.now()));
    }

}