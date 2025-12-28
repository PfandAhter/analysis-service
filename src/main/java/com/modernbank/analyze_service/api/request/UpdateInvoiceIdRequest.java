package com.modernbank.analyze_service.api.request;

import com.modernbank.analyze_service.model.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInvoiceIdRequest {

    private String analysisReportId;

    private String invoiceId;

    private InvoiceStatus status;
}