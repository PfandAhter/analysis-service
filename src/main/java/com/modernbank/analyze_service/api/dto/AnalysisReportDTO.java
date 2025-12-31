package com.modernbank.analyze_service.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnalysisReportDTO {
    private String id;
    private String accountId;
    private String invoiceId;
    private String analysisRange;
    private String overallRiskLevel;
    private String summary;
    private String aiSummary;
    private String keyFindings;
    private String userGuidance;
    private String flaggedTransactionIds;
    private LocalDateTime generatedAt;
    private Integer totalTransactions;
    private String totalOutgoing;
    private String totalIncoming;
    private String netFlow;
    private LocalDateTime createdAt;
}