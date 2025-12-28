package com.modernbank.analyze_service.builder;

import com.modernbank.analyze_service.api.response.AnalysisResponse;
import com.modernbank.analyze_service.model.dto.AggregationResult;
import com.modernbank.analyze_service.model.dto.DetectedPattern;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;
import com.modernbank.analyze_service.model.dto.TransactionStats;
import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.service.ai.AiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Builds user-facing response with professional, non-alarming tone.
 * Integrates with Gemini AI for Turkish language summaries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FrontendResponseBuilder {

    private static final int MAX_KEY_FINDINGS = 5;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    private final AiSummaryService aiSummaryService;

    /**
     * Builds the frontend-facing analysis response with AI-generated Turkish
     * summary.
     */
    public AnalysisResponse build(
            AnalyzeRange analyzeRange,
            RiskLevel riskLevel,
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        // Generate AI summary in Turkish
        String aiSummary = aiSummaryService.generateTurkishSummary(
                analyzeRange, riskLevel, aggregation, patterns, fraudResult);

        TransactionStats statistics = buildStatistics(aggregation);

        return AnalysisResponse.builder()
                .analysisRange(formatAnalyzeRange(analyzeRange))
                .overallRiskLevel(riskLevel.name())
                .summary(buildSummary(riskLevel, aggregation))
                .aiSummary(aiSummary)
                .keyFindings(buildKeyFindings(patterns, fraudResult, aggregation))
                .userGuidance(buildUserGuidance(riskLevel, patterns))
                .flaggedTransactionIds(collectFlaggedTransactions(patterns, fraudResult))
                .generatedAt(LocalDateTime.now())
                .totalTransactions(statistics.getTotalTransactions())
                .totalOutgoing(statistics.getTotalOutgoing())
                .totalIncoming(statistics.getTotalIncoming())
                .netFlow(statistics.getNetFlow())
                .build();
    }

    private String formatAnalyzeRange(AnalyzeRange range) {
        if (range == null)
            return "Bilinmeyen Periyot";
        return switch (range) {
            case LAST_7_DAYS -> "Son 7 Gün";
            case LAST_30_DAYS -> "Son 30 Gün";
        };
    }

    private String buildSummary(RiskLevel riskLevel, AggregationResult aggregation) {
        int txCount = aggregation != null ? aggregation.getTransactionCount() : 0;
        String netFlowDesc = aggregation != null && aggregation.getNetFlow() != null
                ? (aggregation.getNetFlow().compareTo(BigDecimal.ZERO) >= 0 ? "pozitif" : "negatif")
                : "nötr";

        return switch (riskLevel) {
            case LOW -> String.format(
                    "Hesap aktiviteniz normal görünüyor. Bu dönemde %d işlem ve %s net akış tespit edildi.",
                    txCount, netFlowDesc);
            case MEDIUM -> String.format(
                    "%d işlem analiz edildi ve gözden geçirmeye değer bazı örüntüler tespit edildi.",
                    txCount);
            case HIGH -> String.format(
                    "Hesabınızda %d işlem ve gözden geçirmenizi önerdiğimiz bazı olağandışı aktiviteler tespit edildi.",
                    txCount);
        };
    }

    private List<String> buildKeyFindings(
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult,
            AggregationResult aggregation) {

        List<String> findings = new ArrayList<>();

        // Add pattern-based findings in Turkish
        if (patterns != null) {
            for (DetectedPattern pattern : patterns) {
                findings.add(formatPatternFinding(pattern));
                if (findings.size() >= MAX_KEY_FINDINGS)
                    break;
            }
        }

        // Add fraud signal findings if space remains
        if (fraudResult != null && fraudResult.getDominantSignals() != null &&
                findings.size() < MAX_KEY_FINDINGS) {

            for (String signal : fraudResult.getDominantSignals()) {
                if (findings.size() >= MAX_KEY_FINDINGS)
                    break;
                findings.add("Dikkat çeken faktör: " + signal);
            }
        }

        // Add aggregation-based finding if nothing else
        if (findings.isEmpty() && aggregation != null) {
            findings.add(String.format("Bu dönemde %d işlem incelendi.",
                    aggregation.getTransactionCount()));
        }

        return findings.stream().limit(MAX_KEY_FINDINGS).collect(Collectors.toList());
    }

    private String formatPatternFinding(DetectedPattern pattern) {
        // Convert pattern to professional, non-alarming Turkish message
        return switch (pattern.getPatternId()) {
            case "PAT_001" -> "İşlem aktiviteniz önceki döneme göre artış gösterdi.";
            case "PAT_002" -> "Bazı işlemler daha önce kullanmadığınız alıcılara yapıldı.";
            case "PAT_003" -> "Kısa süre içinde birden fazla küçük tutarlı işlem tespit edildi.";
            case "PAT_004" -> "Bazı işlemler tipik aktivitenizden daha yüksek tutarlıydı.";
            case "PAT_005" -> "İşlemlerinizin bir kısmı mesai saatleri dışında gerçekleşti.";
            case "PAT_006" -> "İşlemlerin çoğu yuvarlak tutarlarda yapıldı.";
            default -> pattern.getDescription();
        };
    }

    private String buildUserGuidance(RiskLevel riskLevel, List<DetectedPattern> patterns) {
        return switch (riskLevel) {
            case LOW -> "Herhangi bir işlem gerektirmez. Hesap aktiviteniz normal görünüyor.";
            case MEDIUM ->
                "İşaretlenen işlemleri sizin tarafınızdan yapıldığından emin olmak için gözden geçirmenizi öneririz.";
            case HIGH ->
                "İşaretlenen işlemleri gözden geçirmenizi ve yetkisiz işlem fark ederseniz destek ekibimizle iletişime geçmenizi öneririz.";
        };
    }

    private List<String> collectFlaggedTransactions(
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        List<String> flagged = new ArrayList<>();

        // Collect from high-risk fraud evaluations
        if (fraudResult != null && fraudResult.getHighRiskTransactionIds() != null) {
            flagged.addAll(fraudResult.getHighRiskTransactionIds());
        }

        // Collect from HIGH severity patterns
        if (patterns != null) {
            for (DetectedPattern pattern : patterns) {
                if (pattern.getSeverity() == com.modernbank.analyze_service.model.enums.PatternSeverity.HIGH
                        && pattern.getAffectedTransactionIds() != null) {
                    flagged.addAll(pattern.getAffectedTransactionIds());
                }
            }
        }

        // Return unique IDs
        return flagged.stream().distinct().collect(Collectors.toList());
    }

    private TransactionStats buildStatistics(AggregationResult aggregation) {
        if (aggregation == null) {
            return TransactionStats.builder()
                    .totalTransactions(0)
                    .totalOutgoing("0 TRY")
                    .totalIncoming("0 TRY")
                    .netFlow("0 TRY")
                    .build();
        }

        return TransactionStats.builder()
                .totalTransactions(aggregation.getTransactionCount())
                .totalOutgoing(formatAmount(aggregation.getOutgoingAmount()))
                .totalIncoming(formatAmount(aggregation.getIncomingAmount()))
                .netFlow(formatAmount(aggregation.getNetFlow()))
                .build();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null)
            return "0 TRY";
        return currencyFormatter.format(amount);
    }
}
