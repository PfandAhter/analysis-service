package com.modernbank.analyze_service.service.analysis;

import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.model.dto.DetectedPattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for rule-based pattern detection in transactions.
 */
public interface PatternDetectionService {

    /**
     * Detects behavioral patterns in current period transactions
     * by comparing with previous period.
     *
     * @param currentPeriod     Transactions from current analysis period
     * @param previousPeriod    Transactions from previous period for comparison
     * @param userId            User ID for context
     * @param historicalAverage User's historical average transaction amount
     * @param historicalCount   Total historical transaction count
     * @return List of detected patterns
     */
    List<DetectedPattern> detectPatterns(
            List<EnrichedTransaction> currentPeriod,
            List<EnrichedTransaction> previousPeriod,
            String userId,
            BigDecimal historicalAverage,
            Integer historicalCount);
}
