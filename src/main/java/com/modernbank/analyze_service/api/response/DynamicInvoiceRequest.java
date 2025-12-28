package com.modernbank.analyze_service.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Request payload for Invoice Service.
 * Matches Invoice Service's expected format.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynamicInvoiceRequest {

    private String invoiceId;

    private String userId;

    private String invoiceType;

    @JsonFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss]")
    private LocalDateTime date;

    private Map<String, Object> data;
}
