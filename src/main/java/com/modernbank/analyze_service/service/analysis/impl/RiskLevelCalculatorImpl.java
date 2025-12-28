package com.modernbank.analyze_service.service.analysis.impl;

import com.modernbank.analyze_service.model.dto.AggregationResult;
import com.modernbank.analyze_service.model.dto.DetectedPattern;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;
import com.modernbank.analyze_service.model.enums.PatternSeverity;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.service.analysis.RiskLevelCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of risk level calculator using weighted signals.
 */
@Service
@Slf4j
public class RiskLevelCalculatorImpl implements RiskLevelCalculator {

    private static final int HIGH_RISK_TRANSACTION_THRESHOLD = 3;
    private static final int HIGH_DOMINANT_SIGNAL_THRESHOLD = 2;

    @Override
    public RiskLevel calculateRiskLevel(
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        // Condition 1: HIGH if 3+ high risk transactions
        if (aggregation != null && aggregation.getHighRiskTransactionCount() >= HIGH_RISK_TRANSACTION_THRESHOLD) {
            log.debug("Risk level HIGH: {} high risk transactions >= threshold {}",
                    aggregation.getHighRiskTransactionCount(), HIGH_RISK_TRANSACTION_THRESHOLD);
            return RiskLevel.HIGH;
        }

        // Condition 2: HIGH if any HIGH severity pattern detected
        if (patterns != null && hasPatternWithSeverity(patterns, PatternSeverity.HIGH)) {
            log.debug("Risk level HIGH: HIGH severity pattern detected");
            return RiskLevel.HIGH;
        }

        // Condition 3: HIGH if 2+ dominant fraud signals
        if (fraudResult != null &&
                fraudResult.getDominantSignals() != null &&
                fraudResult.getDominantSignals().size() >= HIGH_DOMINANT_SIGNAL_THRESHOLD) {
            log.debug("Risk level HIGH: {} dominant signals >= threshold {}",
                    fraudResult.getDominantSignals().size(), HIGH_DOMINANT_SIGNAL_THRESHOLD);
            return RiskLevel.HIGH;
        }

        // Condition 4: MEDIUM if 1+ high risk transactions
        if (aggregation != null && aggregation.getHighRiskTransactionCount() >= 1) {
            log.debug("Risk level MEDIUM: {} high risk transactions present",
                    aggregation.getHighRiskTransactionCount());
            return RiskLevel.MEDIUM;
        }

        // Condition 5: MEDIUM if any MEDIUM severity pattern
        if (patterns != null && hasPatternWithSeverity(patterns, PatternSeverity.MEDIUM)) {
            log.debug("Risk level MEDIUM: MEDIUM severity pattern detected");
            return RiskLevel.MEDIUM;
        }

        // Condition 6: MEDIUM if >50% of transactions have fraud evaluation
        if (aggregation != null && aggregation.getTransactionCount() > 0) {
            double evaluatedRatio = (double) aggregation.getEvaluatedTransactionCount() /
                    aggregation.getTransactionCount();
            if (evaluatedRatio > 0.5) {
                log.debug("Risk level MEDIUM: {:.0f}% transactions evaluated > 50%",
                        evaluatedRatio * 100);
                return RiskLevel.MEDIUM;
            }
        }

        log.debug("Risk level LOW: no risk indicators met");
        return RiskLevel.LOW;
    }

    private boolean hasPatternWithSeverity(List<DetectedPattern> patterns, PatternSeverity severity) {
        return patterns.stream()
                .anyMatch(p -> p.getSeverity() == severity);
    }
}
