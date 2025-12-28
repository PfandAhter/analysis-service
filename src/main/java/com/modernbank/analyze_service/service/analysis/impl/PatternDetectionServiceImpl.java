package com.modernbank.analyze_service.service.analysis.impl;

import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.client.model.Transaction;
import com.modernbank.analyze_service.config.ThresholdConfig;
import com.modernbank.analyze_service.model.dto.DetectedPattern;
import com.modernbank.analyze_service.model.enums.PatternSeverity;
import com.modernbank.analyze_service.service.analysis.PatternDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of rule-based pattern detection service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatternDetectionServiceImpl implements PatternDetectionService {

    private final ThresholdConfig thresholdConfig;

    @Override
    public List<DetectedPattern> detectPatterns(
            List<EnrichedTransaction> currentPeriod,
            List<EnrichedTransaction> previousPeriod,
            String userId,
            BigDecimal historicalAverage,
            Integer historicalCount) {

        List<DetectedPattern> detectedPatterns = new ArrayList<>();

        if (currentPeriod == null || currentPeriod.isEmpty()) {
            log.debug("No transactions to analyze for patterns");
            return detectedPatterns;
        }

        // Calculate effective threshold based on user history
        BigDecimal effectiveAverage = calculateEffectiveAverage(historicalAverage, historicalCount);

        // PAT_001: Velocity Spike
        DetectedPattern velocityPattern = detectVelocitySpike(currentPeriod, previousPeriod);
        if (velocityPattern != null) {
            detectedPatterns.add(velocityPattern);
        }

        // PAT_002: New Receivers
        DetectedPattern newReceiverPattern = detectNewReceivers(currentPeriod, previousPeriod, userId);
        if (newReceiverPattern != null) {
            detectedPatterns.add(newReceiverPattern);
        }

        // PAT_003: Micro-Transactions
        DetectedPattern microPattern = detectMicroTransactions(currentPeriod);
        if (microPattern != null) {
            detectedPatterns.add(microPattern);
        }

        // PAT_004: Large Amount Outlier
        DetectedPattern largeAmountPattern = detectLargeAmountOutliers(currentPeriod, effectiveAverage);
        if (largeAmountPattern != null) {
            detectedPatterns.add(largeAmountPattern);
        }

        // PAT_005: Off-Hours Activity
        DetectedPattern offHoursPattern = detectOffHoursActivity(currentPeriod);
        if (offHoursPattern != null) {
            detectedPatterns.add(offHoursPattern);
        }

        // PAT_006: Round Amount Clustering
        DetectedPattern roundAmountPattern = detectRoundAmountClustering(currentPeriod);
        if (roundAmountPattern != null) {
            detectedPatterns.add(roundAmountPattern);
        }

        log.info("Detected {} patterns for user {}", detectedPatterns.size(), userId);
        return detectedPatterns;
    }

    private BigDecimal calculateEffectiveAverage(BigDecimal historicalAverage, Integer historicalCount) {
        if (historicalCount == null || historicalCount < thresholdConfig.getMinimumHistoryForDynamic()) {
            // Use system default
            return thresholdConfig.getDefaultAverageAmount();
        }

        if (historicalAverage == null) {
            return thresholdConfig.getDefaultAverageAmount();
        }

        if (historicalCount >= thresholdConfig.getFullHistoryThreshold()) {
            // Fully trust user's history
            return historicalAverage;
        }

        // Blend: 70% user average + 30% system default
        BigDecimal userWeight = new BigDecimal("0.7");
        BigDecimal systemWeight = new BigDecimal("0.3");

        return historicalAverage.multiply(userWeight)
                .add(thresholdConfig.getDefaultAverageAmount().multiply(systemWeight));
    }

    private DetectedPattern detectVelocitySpike(
            List<EnrichedTransaction> current,
            List<EnrichedTransaction> previous) {

        int currentCount = current.size();
        int previousCount = previous != null ? previous.size() : 0;

        if (previousCount == 0) {
            return null; // Cannot compare without previous data
        }

        double increase = (double) (currentCount - previousCount) / previousCount;

        if (increase > thresholdConfig.getVelocitySpikeThreshold()) {
            List<String> affectedIds = current.stream()
                    .map(e -> e.getTransaction().getTransactionId())
                    .collect(Collectors.toList());

            return DetectedPattern.builder()
                    .patternId("PAT_001")
                    .patternName("Velocity Spike")
                    .severity(increase > 1.0 ? PatternSeverity.HIGH : PatternSeverity.MEDIUM)
                    .description(String.format("Transaction volume increased by %.0f%% compared to previous period",
                            increase * 100))
                    .affectedTransactionIds(affectedIds)
                    .affectedCount(currentCount)
                    .build();
        }

        return null;
    }

    private DetectedPattern detectNewReceivers(
            List<EnrichedTransaction> current,
            List<EnrichedTransaction> previous,
            String userId) {

        // Get previously seen receiver IBANs
        Set<String> previousReceivers = new HashSet<>();
        if (previous != null) {
            for (EnrichedTransaction enriched : previous) {
                Transaction tx = enriched.getTransaction();
                if (tx != null && tx.getReceiverIban() != null) {
                    previousReceivers.add(tx.getReceiverIban());
                }
            }
        }

        // Find new receivers in current period
        List<String> newReceiverTxIds = new ArrayList<>();
        Set<String> newReceiverIbans = new HashSet<>();

        for (EnrichedTransaction enriched : current) {
            Transaction tx = enriched.getTransaction();
            if (tx != null && tx.getReceiverIban() != null) {
                if (!previousReceivers.contains(tx.getReceiverIban())) {
                    newReceiverTxIds.add(tx.getTransactionId());
                    newReceiverIbans.add(tx.getReceiverIban());
                }
            }
        }

        double newReceiverRatio = current.isEmpty() ? 0 : (double) newReceiverTxIds.size() / current.size();

        if (newReceiverRatio > thresholdConfig.getNewReceiverThreshold()) {
            return DetectedPattern.builder()
                    .patternId("PAT_002")
                    .patternName("New Receivers")
                    .severity(newReceiverRatio > 0.5 ? PatternSeverity.MEDIUM : PatternSeverity.LOW)
                    .description(String.format("%.0f%% of transactions were to new recipients (%d unique)",
                            newReceiverRatio * 100, newReceiverIbans.size()))
                    .affectedTransactionIds(newReceiverTxIds)
                    .affectedCount(newReceiverTxIds.size())
                    .build();
        }

        return null;
    }

    private DetectedPattern detectMicroTransactions(List<EnrichedTransaction> transactions) {
        BigDecimal threshold = thresholdConfig.getMicroTransactionLimit();

        // Group micro-transactions by 24-hour windows
        Map<String, List<String>> microTxByDay = new HashMap<>();

        for (EnrichedTransaction enriched : transactions) {
            Transaction tx = enriched.getTransaction();
            if (tx != null && tx.getAmount() != null &&
                    tx.getAmount().compareTo(threshold) < 0) {

                String dayKey = tx.getCreatedAt() != null ? tx.getCreatedAt().toLocalDate().toString() : "unknown";

                microTxByDay.computeIfAbsent(dayKey, k -> new ArrayList<>())
                        .add(tx.getTransactionId());
            }
        }

        // Check if any day has more than threshold count
        List<String> affectedIds = new ArrayList<>();
        for (List<String> dayTxs : microTxByDay.values()) {
            if (dayTxs.size() > thresholdConfig.getMicroTransactionCountThreshold()) {
                affectedIds.addAll(dayTxs);
            }
        }

        if (!affectedIds.isEmpty()) {
            return DetectedPattern.builder()
                    .patternId("PAT_003")
                    .patternName("Micro-Transactions")
                    .severity(affectedIds.size() > 10 ? PatternSeverity.HIGH : PatternSeverity.MEDIUM)
                    .description(
                            String.format("Detected %d small transactions (under %s) concentrated in short periods",
                                    affectedIds.size(), threshold))
                    .affectedTransactionIds(affectedIds)
                    .affectedCount(affectedIds.size())
                    .build();
        }

        return null;
    }

    private DetectedPattern detectLargeAmountOutliers(
            List<EnrichedTransaction> transactions,
            BigDecimal averageAmount) {

        BigDecimal threshold = averageAmount.multiply(
                BigDecimal.valueOf(thresholdConfig.getLargeTransactionMultiplier()));

        List<String> outlierIds = new ArrayList<>();

        for (EnrichedTransaction enriched : transactions) {
            Transaction tx = enriched.getTransaction();
            if (tx != null && tx.getAmount() != null &&
                    tx.getAmount().compareTo(threshold) > 0) {
                outlierIds.add(tx.getTransactionId());
            }
        }

        if (!outlierIds.isEmpty()) {
            return DetectedPattern.builder()
                    .patternId("PAT_004")
                    .patternName("Large Amount Outlier")
                    .severity(outlierIds.size() > 2 ? PatternSeverity.HIGH : PatternSeverity.MEDIUM)
                    .description(String.format("Detected %d transactions exceeding %.1fx the typical amount",
                            outlierIds.size(), thresholdConfig.getLargeTransactionMultiplier()))
                    .affectedTransactionIds(outlierIds)
                    .affectedCount(outlierIds.size())
                    .build();
        }

        return null;
    }

    private DetectedPattern detectOffHoursActivity(List<EnrichedTransaction> transactions) {
        List<String> offHoursIds = new ArrayList<>();

        for (EnrichedTransaction enriched : transactions) {
            Transaction tx = enriched.getTransaction();
            if (tx != null && tx.getCreatedAt() != null) {
                LocalDateTime dateTime = tx.getCreatedAt();
                LocalTime time = dateTime.toLocalTime();
                int dayOfWeek = dateTime.getDayOfWeek().getValue();

                // Weekend (Saturday = 6, Sunday = 7) or outside 09:00-18:00
                boolean isWeekend = dayOfWeek >= 6;
                boolean isOffHours = time.isBefore(LocalTime.of(9, 0)) ||
                        time.isAfter(LocalTime.of(18, 0));

                if (isWeekend || isOffHours) {
                    offHoursIds.add(tx.getTransactionId());
                }
            }
        }

        double offHoursRatio = transactions.isEmpty() ? 0 : (double) offHoursIds.size() / transactions.size();

        if (offHoursRatio > thresholdConfig.getOffHoursThreshold()) {
            return DetectedPattern.builder()
                    .patternId("PAT_005")
                    .patternName("Off-Hours Activity")
                    .severity(offHoursRatio > 0.6 ? PatternSeverity.MEDIUM : PatternSeverity.LOW)
                    .description(String.format("%.0f%% of transactions occurred outside business hours or on weekends",
                            offHoursRatio * 100))
                    .affectedTransactionIds(offHoursIds)
                    .affectedCount(offHoursIds.size())
                    .build();
        }

        return null;
    }

    private DetectedPattern detectRoundAmountClustering(List<EnrichedTransaction> transactions) {
        List<String> roundAmountIds = new ArrayList<>();

        for (EnrichedTransaction enriched : transactions) {
            Transaction tx = enriched.getTransaction();
            if (tx != null && tx.getAmount() != null) {
                BigDecimal amount = tx.getAmount();
                // Check if amount is a round number (divisible by 100 with no remainder)
                if (amount.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) == 0 &&
                        amount.compareTo(BigDecimal.ZERO) > 0) {
                    roundAmountIds.add(tx.getTransactionId());
                }
            }
        }

        double roundRatio = transactions.isEmpty() ? 0 : (double) roundAmountIds.size() / transactions.size();

        if (roundRatio > thresholdConfig.getRoundAmountThreshold()) {
            return DetectedPattern.builder()
                    .patternId("PAT_006")
                    .patternName("Round Amount Clustering")
                    .severity(PatternSeverity.LOW)
                    .description(String.format("%.0f%% of transactions are round amounts (100, 500, 1000, etc.)",
                            roundRatio * 100))
                    .affectedTransactionIds(roundAmountIds)
                    .affectedCount(roundAmountIds.size())
                    .build();
        }

        return null;
    }
}
