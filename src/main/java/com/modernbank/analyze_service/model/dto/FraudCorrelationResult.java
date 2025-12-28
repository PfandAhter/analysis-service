package com.modernbank.analyze_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Result of fraud signal correlation analysis.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudCorrelationResult {

    /**
     * Human-readable dominant fraud signals (max 5).
     * These are derived from feature importance analysis.
     */
    private List<String> dominantSignals;

    /**
     * List of transaction IDs marked as HIGH risk.
     */
    private List<String> highRiskTransactionIds;

    /**
     * Internal signal distribution for analysis (not exposed to end user).
     * Maps signal name to occurrence count.
     */
    private Map<String, Integer> signalDistribution;

    /**
     * Count of transactions with fraud evaluation present.
     */
    private int evaluatedCount;
}
