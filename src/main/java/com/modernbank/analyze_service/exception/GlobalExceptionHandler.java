package com.modernbank.analyze_service.exception;

import com.modernbank.analyze_service.api.response.BaseResponse;
import com.modernbank.analyze_service.constant.HeaderKey;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Analysis Service.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleExternalServiceUnavailable(
            ExternalServiceUnavailableException ex) {

        log.error("External service unavailable: {}", ex.getServiceName(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Unable to complete analysis due to external service unavailability");
        response.put("service", ex.getServiceName());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }


    @ExceptionHandler({RemoteDirectException.class})
    public ResponseEntity<BaseResponse> handleBusinessException(RemoteDirectException e, HttpServletRequest request) {
        logError(e, request);

        return ResponseEntity.status(e.getHttpStatus()).body(createRemoteDirectErrorResponseBody(e.getOriginalErrorCode(), e.getOriginalMessage()));
    }

    @ExceptionHandler(AnalysisException.class)
    public ResponseEntity<Map<String, Object>> handleAnalysisException(AnalysisException ex) {
        log.error("Analysis error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Analysis Failed");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred during analysis");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private BaseResponse createRemoteDirectErrorResponseBody(String error, String description) {
        return new BaseResponse("FAILED", error, description);
    }

    private void logError(Exception exception, HttpServletRequest httpServletRequest) {
        log.error("TraceId {} got error, Error: {} ",
                httpServletRequest.getHeader(HeaderKey.CORRELATION_ID),
                exception.getMessage());
    }
}
