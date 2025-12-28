package com.modernbank.analyze_service.client.model;

import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model from Transaction Service containing enriched transactions
 * for both current and previous periods for comparison analysis.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAnalyzeModel {

    /**
     * The requested analysis range.
     */
    private AnalyzeRange analyzeRange;

    /**
     * Transactions from the current analysis period.
     */
    private List<EnrichedTransaction> currentPeriodTransactions;

    /**
     * Transactions from the previous period (for comparison/trend analysis).
     */
    private List<EnrichedTransaction> previousPeriodTransactions;

    /**
     * Total historical transaction count for dynamic threshold calculation.
     */
    private Integer totalHistoricalTransactionCount;

    /**
     * User's historical average transaction amount.
     */
    private Double historicalAverageAmount;
}
