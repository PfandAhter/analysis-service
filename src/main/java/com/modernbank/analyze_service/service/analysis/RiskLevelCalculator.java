package com.modernbank.analyze_service.service.analysis;

import com.modernbank.analyze_service.model.dto.AggregationResult;
import com.modernbank.analyze_service.model.dto.DetectedPattern;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;
import com.modernbank.analyze_service.model.enums.RiskLevel;

import java.util.List;

/**
 * Service for calculating overall risk level based on analysis results.
 */
public interface RiskLevelCalculator {

    /**
     * Calculates the overall risk level based on aggregation, patterns, and fraud
     * signals.
     *
     * @param aggregation Transaction aggregation results
     * @param patterns    Detected patterns
     * @param fraudResult Fraud correlation results
     * @return The calculated risk level
     */
    RiskLevel calculateRiskLevel(
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult);
}
