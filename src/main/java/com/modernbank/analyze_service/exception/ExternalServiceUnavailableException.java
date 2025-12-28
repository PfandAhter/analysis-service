package com.modernbank.analyze_service.exception;

/**
 * Exception thrown when an external service is unavailable.
 */
public class ExternalServiceUnavailableException extends AnalysisException {

    private final String serviceName;

    public ExternalServiceUnavailableException(String serviceName) {
        super("External service unavailable: " + serviceName);
        this.serviceName = serviceName;
    }

    public ExternalServiceUnavailableException(String serviceName, Throwable cause) {
        super("External service unavailable: " + serviceName, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
