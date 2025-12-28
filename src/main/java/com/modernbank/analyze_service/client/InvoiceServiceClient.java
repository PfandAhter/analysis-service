package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.api.response.DynamicInvoiceRequest;
import com.modernbank.analyze_service.client.model.InvoiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Invoice Service.
 * Sends analysis report generation requests after analysis is complete.
 */
@FeignClient(name = "invoice-service", url = "${service.invoice.url}")
public interface InvoiceServiceClient {

    /**
     * Requests invoice generation from Invoice Service.
     *
     * @param request       The invoice request payload
     * @param token         Authorization token
     * @param correlationId Correlation ID for request tracing
     * @return InvoiceResponse with request ID and estimated completion time
     */
    @PostMapping("/api/v1/invoices/generate")
    InvoiceResponse generateInvoice(
            @RequestBody DynamicInvoiceRequest request,
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Correlation-ID") String correlationId);
}
