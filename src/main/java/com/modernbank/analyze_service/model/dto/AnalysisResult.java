package com.modernbank.analyze_service.model.dto;

import com.modernbank.analyze_service.api.response.AnalysisResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {
    // private AnalysisResponse frontendResponse;

    private String analysisReportId;

    private String invoiceRequestId;

    private String invoiceStatus;

    private LocalDateTime estimatedCompletionDate;

    private String invoiceMessage;
}
