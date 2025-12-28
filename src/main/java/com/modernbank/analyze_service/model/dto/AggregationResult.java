package com.modernbank.analyze_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Result of transaction aggregation analysis.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregationResult {

    /**
     * Total number of transactions in the analysis period.
     */
    private int transactionCount;

    /**
     * Total outgoing amount (user as sender).
     */
    private BigDecimal outgoingAmount;

    /**
     * Total incoming amount (user as receiver).
     */
    private BigDecimal incomingAmount;

    /**
     * Count of transactions that have fraud evaluation data.
     */
    private int evaluatedTransactionCount;

    /**
     * Count of transactions marked as HIGH risk.
     */
    private int highRiskTransactionCount;

    /**
     * Net flow (incoming - outgoing).
     */
    private BigDecimal netFlow;

    /**
     * Average transaction amount for this period.
     */
    private BigDecimal averageAmount;
}
