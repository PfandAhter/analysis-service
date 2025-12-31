package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.api.response.DynamicInvoiceRequest;
import com.modernbank.analyze_service.client.model.InvoiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.modernbank.analyze_service.constant.HeaderKey.*;

/**
 * Feign client for Invoice Service.
 * Sends analysis report generation requests after analysis is complete.
 */
@FeignClient(name = "invoice-service", url = "${service.invoice.url}")
public interface InvoiceServiceClient {

    @PostMapping("/api/v1/invoices/generate")
    InvoiceResponse generateInvoice(
            @RequestBody DynamicInvoiceRequest request,
            @RequestHeader(USER_ID) String userId,
            @RequestHeader(USER_ROLE) String role,
            @RequestHeader(AUTHORIZATION_TOKEN) String token
    );
}