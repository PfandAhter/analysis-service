package com.modernbank.analyze_service.service.analysis.impl;

import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.client.model.FraudEvaluation;
import com.modernbank.analyze_service.client.model.Transaction;
import com.modernbank.analyze_service.model.dto.AggregationResult;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.service.analysis.AggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implementation of transaction aggregation service.
 */
@Service
@Slf4j
public class AggregationServiceImpl implements AggregationService {

    @Override
    public AggregationResult aggregate(List<EnrichedTransaction> transactions, String userId) {
        if (transactions == null || transactions.isEmpty()) {
            log.debug("No transactions to aggregate for user {}", userId);
            return createEmptyResult();
        }

        BigDecimal outgoingAmount = BigDecimal.ZERO;
        BigDecimal incomingAmount = BigDecimal.ZERO;
        int evaluatedCount = 0;
        int highRiskCount = 0;

        for (EnrichedTransaction enriched : transactions) {
            Transaction tx = enriched.getTransaction();
            if (tx == null) {
                continue;
            }

            BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;

            // Determine if user is sender or receiver
            if (isUserSender(tx, userId)) {
                outgoingAmount = outgoingAmount.add(amount);
            } else {
                incomingAmount = incomingAmount.add(amount);
            }

            // Check fraud evaluation
            FraudEvaluation fraud = enriched.getFraudEvaluation();
            if (fraud != null) {
                evaluatedCount++;
                if (fraud.getRiskLevel() == RiskLevel.HIGH) {
                    highRiskCount++;
                }
            }
        }

        int transactionCount = transactions.size();
        BigDecimal netFlow = incomingAmount.subtract(outgoingAmount);
        BigDecimal averageAmount = transactionCount > 0
                ? outgoingAmount.add(incomingAmount).divide(BigDecimal.valueOf(transactionCount), 2,
                        RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.debug("Aggregation complete for user {}: {} transactions, {} outgoing, {} incoming, {} high risk",
                userId, transactionCount, outgoingAmount, incomingAmount, highRiskCount);

        return AggregationResult.builder()
                .transactionCount(transactionCount)
                .outgoingAmount(outgoingAmount)
                .incomingAmount(incomingAmount)
                .evaluatedTransactionCount(evaluatedCount)
                .highRiskTransactionCount(highRiskCount)
                .netFlow(netFlow)
                .averageAmount(averageAmount)
                .build();
    }

    private boolean isUserSender(Transaction tx, String userId) {
        // Check if user is the sender based on userId
        return userId.equals(tx.getUserId());
    }

    private AggregationResult createEmptyResult() {
        return AggregationResult.builder()
                .transactionCount(0)
                .outgoingAmount(BigDecimal.ZERO)
                .incomingAmount(BigDecimal.ZERO)
                .evaluatedTransactionCount(0)
                .highRiskTransactionCount(0)
                .netFlow(BigDecimal.ZERO)
                .averageAmount(BigDecimal.ZERO)
                .build();
    }
}
