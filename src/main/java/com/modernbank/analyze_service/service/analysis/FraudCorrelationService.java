package com.modernbank.analyze_service.service.analysis;

import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;

import java.util.List;

/**
 * Service for correlating fraud signals across transactions.
 */
public interface FraudCorrelationService {

    /**
     * Correlates fraud evaluation data across transactions to identify
     * dominant fraud signals and high-risk transactions.
     *
     * @param transactions List of enriched transactions
     * @return Fraud correlation result with dominant signals
     */
    FraudCorrelationResult correlate(List<EnrichedTransaction> transactions);
}
