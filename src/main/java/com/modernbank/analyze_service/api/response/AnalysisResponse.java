package com.modernbank.analyze_service.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Frontend-facing response for transaction analysis.
 * Designed to be human-readable and non-alarming.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponse {

    /**
     * Human-readable analysis range (e.g., "Last 7 Days").
     */
    private String analysisRange;


    private String invoiceId;
    /**
     * Overall risk level: LOW, MEDIUM, or HIGH.
     */
    private String overallRiskLevel;

    /**
     * Brief 1-2 sentence summary of the analysis.
     */
    private String summary;

    /**
     * AI-generated Turkish language summary for user display.
     */
    private String aiSummary;

    /**
     * Key findings in bullet point format (max 5).
     */
    private List<String> keyFindings;

    /**
     * User guidance - what the user should be aware of.
     */
    private String userGuidance;

    /**
     * Optional list of transaction IDs worth reviewing.
     */
    private List<String> flaggedTransactionIds;

    /**
     * Timestamp when analysis was generated.
     */
    private LocalDateTime generatedAt;

    /**
     * Transaction statistics for display.
     */
    private int totalTransactions;
    private String totalOutgoing;
    private String totalIncoming;
    private String netFlow;

}
