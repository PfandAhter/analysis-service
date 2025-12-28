package com.modernbank.analyze_service.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernbank.analyze_service.model.dto.AggregationResult;
import com.modernbank.analyze_service.model.dto.DetectedPattern;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;
import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.service.ai.AiSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiAiSummaryService implements AiSummaryService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeminiAiSummaryService(
            @Value("${gemini.api.key}") String apiKey,
            WebClient.Builder webClientBuilder) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateTurkishSummary(
            AnalyzeRange analyzeRange,
            RiskLevel riskLevel,
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        try {
            String prompt = buildPrompt(analyzeRange, riskLevel, aggregation, patterns, fraudResult);
            return callGeminiApi(prompt);
        } catch (Exception e) {
            log.error("Failed to generate AI summary: {}", e.getMessage());
            return generateFallbackSummary(analyzeRange, riskLevel, aggregation);
        }
    }

    private String buildPrompt(
            AnalyzeRange analyzeRange,
            RiskLevel riskLevel,
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult) {

        StringBuilder sb = new StringBuilder();
        sb.append("Sen bir banka analiz asistanısın. Aşağıdaki işlem analiz sonuçlarını ");
        sb.append("Türkçe olarak özetle. Profesyonel, sakin ve korku yaratmayan bir dil kullan. ");
        sb.append("Kullanıcıya yardımcı ol ve gerekirse önerilerde bulun. ");
        sb.append("Özet 3-5 cümle olsun.\n\n");

        sb.append("ANALİZ VERİLERİ:\n");

        // Time range
        String rangeText = analyzeRange == AnalyzeRange.LAST_7_DAYS ? "Son 7 gün" : "Son 30 gün";
        sb.append("- Analiz Periyodu: ").append(rangeText).append("\n");

        // Risk level
        String riskText = switch (riskLevel) {
            case LOW -> "DÜŞÜK";
            case MEDIUM -> "ORTA";
            case HIGH -> "YÜKSEK";
        };
        sb.append("- Genel Risk Seviyesi: ").append(riskText).append("\n");

        // Aggregation
        if (aggregation != null) {
            sb.append("- Toplam İşlem Sayısı: ").append(aggregation.getTransactionCount()).append("\n");
            sb.append("- Giden Toplam Tutar: ").append(aggregation.getOutgoingAmount()).append(" TRY\n");
            sb.append("- Gelen Toplam Tutar: ").append(aggregation.getIncomingAmount()).append(" TRY\n");
            sb.append("- Net Akış: ").append(aggregation.getNetFlow()).append(" TRY\n");
            sb.append("- Yüksek Riskli İşlem Sayısı: ").append(aggregation.getHighRiskTransactionCount()).append("\n");
        }

        // Patterns
        if (patterns != null && !patterns.isEmpty()) {
            sb.append("- Tespit Edilen Örüntüler:\n");
            for (DetectedPattern pattern : patterns) {
                String patternNameTr = translatePatternName(pattern.getPatternName());
                sb.append("  * ").append(patternNameTr)
                        .append(" (").append(pattern.getAffectedCount()).append(" işlem etkilendi)\n");
            }
        }

        // Fraud signals
        if (fraudResult != null && fraudResult.getDominantSignals() != null &&
                !fraudResult.getDominantSignals().isEmpty()) {
            sb.append("- Öne Çıkan Risk Faktörleri: ");
            sb.append(String.join(", ", fraudResult.getDominantSignals())).append("\n");
        }

        sb.append("\nBu verilere göre kullanıcıya Türkçe bir özet yaz. ");
        sb.append("Teknik terimler kullanma, günlük dilde açıkla.");

        return sb.toString();
    }

    private String translatePatternName(String patternName) {
        return switch (patternName) {
            case "Velocity Spike" -> "İşlem hızında artış";
            case "New Receivers" -> "Yeni alıcılara transfer";
            case "Micro-Transactions" -> "Küçük tutarlı çoklu işlemler";
            case "Large Amount Outlier" -> "Normalin üzerinde büyük tutarlı işlem";
            case "Off-Hours Activity" -> "Mesai saatleri dışında işlem";
            case "Round Amount Clustering" -> "Yuvarlak tutarlı işlemler";
            default -> patternName;
        };
    }

    private String callGeminiApi(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();

            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            // Add generation config for better Turkish output
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 500);
            requestBody.put("generationConfig", generationConfig);

            String response = webClient.post()
                    .uri(GEMINI_API_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed", e);
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            log.warn("Could not extract text from Gemini response: {}", response);
            return "Analiz özeti oluşturulamadı.";

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return "Analiz özeti oluşturulamadı.";
        }
    }

    private String generateFallbackSummary(
            AnalyzeRange analyzeRange,
            RiskLevel riskLevel,
            AggregationResult aggregation) {

        String rangeText = analyzeRange == AnalyzeRange.LAST_7_DAYS ? "son 7 günlük" : "son 30 günlük";
        int txCount = aggregation != null ? aggregation.getTransactionCount() : 0;

        String riskText = switch (riskLevel) {
            case LOW -> "düşük";
            case MEDIUM -> "orta";
            case HIGH -> "yüksek";
        };

        return String.format(
                "%s işlemlerinizi inceledik. Toplam %d işlem gerçekleştirdiniz. " +
                        "Genel risk seviyeniz %s olarak değerlendirilmiştir. " +
                        "Detaylı bilgi için işlem geçmişinizi inceleyebilirsiniz.",
                rangeText.substring(0, 1).toUpperCase() + rangeText.substring(1),
                txCount,
                riskText);
    }
}
