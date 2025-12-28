package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.client.model.TransactionAnalyzeModel;
import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Transaction Service.
 * Fetches transaction data with fraud evaluations for analysis.
 */
@FeignClient(name = "transaction-service", url = "${service.transaction.url}")
public interface TransactionServiceClient {

    /**
     * Fetches transactions for analysis including both current and previous period
     * data.
     *
     * @param userId        User ID to fetch transactions for
     * @param analyzeRange  The analysis time range (LAST_7_DAYS or LAST_30_DAYS)
     * @param token         Authorization token
     * @param correlationId Correlation ID for request tracing
     * @return TransactionAnalyzeModel containing enriched transactions
     */
    @GetMapping("/api/v1/transactions/analyze")
    TransactionAnalyzeModel getTransactionsForAnalysis(
            @RequestParam("userId") String userId,
            @RequestParam("analyzeRange") AnalyzeRange analyzeRange,
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Correlation-ID") String correlationId);
}
