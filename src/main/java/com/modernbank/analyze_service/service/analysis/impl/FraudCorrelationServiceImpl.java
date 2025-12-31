package com.modernbank.analyze_service.service.analysis.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.client.model.FraudEvaluation;
import com.modernbank.analyze_service.client.model.Transaction;
import com.modernbank.analyze_service.config.FeatureNameMapper;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.service.analysis.FraudCorrelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of fraud signal correlation service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudCorrelationServiceImpl implements FraudCorrelationService {

    private static final int MAX_DOMINANT_SIGNALS = 5;
    private static final double IMPORTANCE_THRESHOLD = 0.1;

    private final FeatureNameMapper featureNameMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FraudCorrelationResult correlate(List<EnrichedTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return createEmptyResult();
        }

        Map<String, Integer> signalDistribution = new HashMap<>();
        List<String> highRiskTransactionIds = new ArrayList<>();
        int evaluatedCount = 0;

        for (EnrichedTransaction enriched : transactions) {
            FraudEvaluation fraud = enriched.getFraudEvaluation();
            if (fraud == null) {
                continue;
            }

            evaluatedCount++;

            // Track high risk transactions
            if (fraud.getRiskLevel() == RiskLevel.HIGH) {
                Transaction tx = enriched.getTransaction();
                if (tx != null && tx.getTransactionId() != null) {
                    highRiskTransactionIds.add(tx.getTransactionId());
                }
            }

            // Parse and aggregate feature importance
            Map<String, Double> featureImportance = parseFeatureImportance(fraud.getFeatureImportance());
            for (Map.Entry<String, Double> entry : featureImportance.entrySet()) {
                if (entry.getValue() >= IMPORTANCE_THRESHOLD) {
                    signalDistribution.merge(entry.getKey(), 1, Integer::sum);
                }
            }
        }

        // Get dominant signals (top N by occurrence)
        List<String> dominantSignals = signalDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(MAX_DOMINANT_SIGNALS)
                .map(entry -> featureNameMapper.mapToHumanReadable(entry.getKey()))
                .collect(Collectors.toList());

        log.debug("Fraud correlation complete: {} evaluated, {} high risk, {} dominant signals",
                evaluatedCount, highRiskTransactionIds.size(), dominantSignals.size());

        return FraudCorrelationResult.builder()
                .dominantSignals(dominantSignals)
                .highRiskTransactionIds(highRiskTransactionIds)
                .signalDistribution(signalDistribution)
                .evaluatedCount(evaluatedCount)
                .build();
    }

    private Map<String, Double> parseFeatureImportance(String featureImportanceJson) {
        if (featureImportanceJson == null || featureImportanceJson.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(featureImportanceJson,
                    new TypeReference<Map<String, Double>>() {
                    });
        } catch (Exception e) {
            log.warn("Failed to parse feature importance JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private FraudCorrelationResult createEmptyResult() {
        return FraudCorrelationResult.builder()
                .dominantSignals(Collections.emptyList())
                .highRiskTransactionIds(Collections.emptyList())
                .signalDistribution(Collections.emptyMap())
                .evaluatedCount(0)
                .build();
    }
}
