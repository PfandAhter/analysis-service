package com.modernbank.analyze_service.builder;

import com.modernbank.analyze_service.api.response.DynamicInvoiceRequest;
import com.modernbank.analyze_service.client.model.Account;
import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.client.model.Transaction;
import com.modernbank.analyze_service.model.dto.*;
import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds structured, deterministic payload for Invoice Service.
 */
@Component
@Slf4j
public class InvoicePayloadBuilder {

    private static final String INVOICE_TYPE = "TRANSACTION_ANALYSIS_REPORT";

    public DynamicInvoiceRequest build(
            String analysisReportId,
            String userId,
            AnalyzeRange analyzeRange,
            RiskLevel riskLevel,
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult,
            List<EnrichedTransaction> transactions,
            List<Account> accounts,
            String aiSummary) {

        LocalDate now = LocalDate.now();
        LocalDate periodStart = calculatePeriodStart(analyzeRange, now);

        Map<String, Object> data = buildInvoiceDataMap(
                analysisReportId,
                analyzeRange, periodStart, now, riskLevel,
                aggregation, patterns, fraudResult, transactions, accounts, aiSummary);

        return DynamicInvoiceRequest.builder()
                .invoiceId(UUID.randomUUID().toString())
                .userId(userId)
                .invoiceType(INVOICE_TYPE)
                .date(LocalDateTime.now())
                .data(data)
                .build();
    }

    private LocalDate calculatePeriodStart(AnalyzeRange range, LocalDate endDate) {
        if (range == null)
            return endDate.minusDays(7);
        return switch (range) {
            case LAST_7_DAYS -> endDate.minusDays(7);
            case LAST_30_DAYS -> endDate.minusDays(30);
        };
    }

    private Map<String, Object> buildInvoiceDataMap(
            String analysisReportId,
            AnalyzeRange analyzeRange,
            LocalDate periodStart,
            LocalDate periodEnd,
            RiskLevel riskLevel,
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult,
            List<EnrichedTransaction> transactions,
            List<Account> accounts,
            String aiSummary) {

        Map<String, Object> data = new LinkedHashMap<>(); // Preserve insertion order for determinism

        // AI-generated Turkish summary at the top
        if (aiSummary != null && !aiSummary.isEmpty()) {
            data.put("aiSummary", aiSummary);
        }

        data.put("analysisReportId", analysisReportId);
        data.put("analysisRange", analyzeRange != null ? analyzeRange.name() : "UNKNOWN");
        data.put("periodStart", periodStart.toString());
        data.put("periodEnd", periodEnd.toString());
        data.put("transactionCount", aggregation != null ? aggregation.getTransactionCount() : 0);
        data.put("highRiskTransactionCount", aggregation != null ? aggregation.getHighRiskTransactionCount() : 0);
        data.put("totalOutgoingAmount", aggregation != null ? aggregation.getOutgoingAmount().toString() : "0");
        data.put("totalIncomingAmount", aggregation != null ? aggregation.getIncomingAmount().toString() : "0");
        data.put("overallRiskLevel", riskLevel.name());
        data.put("dominantRiskReasons", buildDominantReasons(patterns, fraudResult));
        data.put("flaggedTransactionIds", collectFlaggedIds(patterns, fraudResult));
        data.put("detectedPatterns", buildPatternSummaryMaps(patterns));

        List<Map<String, Object>> accountSummaries = buildAccountSummaryMaps(transactions, accounts);
        if (accountSummaries != null && !accountSummaries.isEmpty()) {
            data.put("accountSummaries", accountSummaries);
        }

        return data;
    }

    private List<String> buildDominantReasons(
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        List<String> reasons = new ArrayList<>();

        if (patterns != null) {
            for (DetectedPattern pattern : patterns) {
                reasons.add(pattern.getPatternName() + ": " + pattern.getDescription());
            }
        }

        if (fraudResult != null && fraudResult.getDominantSignals() != null) {
            reasons.addAll(fraudResult.getDominantSignals());
        }

        Collections.sort(reasons);
        return reasons;
    }

    private List<String> collectFlaggedIds(
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        Set<String> flagged = new TreeSet<>();

        if (fraudResult != null && fraudResult.getHighRiskTransactionIds() != null) {
            fraudResult.getHighRiskTransactionIds().stream()
                    .filter(Objects::nonNull)
                    .forEach(flagged::add);
        }

        if (patterns != null) {
            for (DetectedPattern pattern : patterns) {
                if (pattern.getAffectedTransactionIds() != null) {
                    pattern.getAffectedTransactionIds().stream()
                            .filter(Objects::nonNull)
                            .forEach(flagged::add);
                }
            }
        }

        return new ArrayList<>(flagged);
    }

    private List<Map<String, Object>> buildPatternSummaryMaps(List<DetectedPattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return Collections.emptyList();
        }

        return patterns.stream()
                .sorted(Comparator.comparing(DetectedPattern::getPatternName))
                .map(p -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("patternName", p.getPatternName());
                    map.put("severity", p.getSeverity().name());
                    map.put("affectedCount", p.getAffectedCount());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildAccountSummaryMaps(
            List<EnrichedTransaction> transactions,
            List<Account> accounts) {

        if (accounts == null || accounts.isEmpty()) {
            return null;
        }

        Map<String, AccountSummary.AccountSummaryBuilder> summaryBuilders = new HashMap<>();

        for (Account account : accounts) {
            summaryBuilders.put(account.getAccountId(), AccountSummary.builder()
                    .accountId(account.getAccountId())
                    .iban(account.getIban())
                    .transactionCount(0)
                    .netFlow(BigDecimal.ZERO)
                    .outgoingAmount(BigDecimal.ZERO)
                    .incomingAmount(BigDecimal.ZERO));
        }

        if (transactions != null) {
            for (EnrichedTransaction enriched : transactions) {
                Transaction tx = enriched.getTransaction();
                if (tx == null)
                    continue;

                String senderAccountId = tx.getSenderAccountId();
                String receiverAccountId = tx.getReceiverAccountId();
                BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;

                if (senderAccountId != null && summaryBuilders.containsKey(senderAccountId)) {
                    AccountSummary current = summaryBuilders.get(senderAccountId).build();
                    summaryBuilders.put(senderAccountId, AccountSummary.builder()
                            .accountId(current.getAccountId())
                            .iban(current.getIban())
                            .transactionCount(current.getTransactionCount() + 1)
                            .outgoingAmount(current.getOutgoingAmount().add(amount))
                            .incomingAmount(current.getIncomingAmount())
                            .netFlow(current.getNetFlow().subtract(amount)));
                }

                if (receiverAccountId != null && summaryBuilders.containsKey(receiverAccountId)) {
                    AccountSummary current = summaryBuilders.get(receiverAccountId).build();
                    summaryBuilders.put(receiverAccountId, AccountSummary.builder()
                            .accountId(current.getAccountId())
                            .iban(current.getIban())
                            .transactionCount(current.getTransactionCount() + 1)
                            .outgoingAmount(current.getOutgoingAmount())
                            .incomingAmount(current.getIncomingAmount().add(amount))
                            .netFlow(current.getNetFlow().add(amount)));
                }
            }
        }

        return summaryBuilders.values().stream()
                .map(b -> b.build())
                .sorted(Comparator.comparing(AccountSummary::getAccountId))
                .map(summary -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("accountId", summary.getAccountId());
                    map.put("iban", summary.getIban());
                    map.put("transactionCount", summary.getTransactionCount());
                    map.put("outgoingAmount", summary.getOutgoingAmount().toString());
                    map.put("incomingAmount", summary.getIncomingAmount().toString());
                    map.put("netFlow", summary.getNetFlow().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
