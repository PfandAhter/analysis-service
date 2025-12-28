package com.modernbank.analyze_service.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enriched transaction combining transaction data with optional fraud
 * evaluation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrichedTransaction {

    /**
     * The transaction data - single source of truth.
     */
    private Transaction transaction;

    /**
     * Optional fraud evaluation - advisory only, may be null.
     */
    private FraudEvaluation fraudEvaluation;
}
