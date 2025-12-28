package com.modernbank.analyze_service.service.analysis;

import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.model.dto.AggregationResult;

import java.util.List;

/**
 * Service for aggregating transaction data.
 */
public interface AggregationService {

    /**
     * Aggregates transaction data to produce summary statistics.
     *
     * @param transactions List of enriched transactions
     * @param userId       User ID for determining sender/receiver
     * @return Aggregation result with counts and totals
     */
    AggregationResult aggregate(List<EnrichedTransaction> transactions, String userId);
}
