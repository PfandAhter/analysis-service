package com.modernbank.analyze_service.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response from Invoice Service after invoice generation request.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {

    private String requestId;

    private String invoiceType;

    private String invoiceStatus;

    private LocalDateTime requestTime;

    private LocalDateTime estimatedCompletionDate;
}
