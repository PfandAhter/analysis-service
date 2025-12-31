package com.modernbank.analyze_service.orchestrator;

import com.modernbank.analyze_service.api.request.AnalyzeTransactionRequest;
import com.modernbank.analyze_service.api.request.BaseRequest;
import com.modernbank.analyze_service.api.response.DynamicInvoiceRequest;
import com.modernbank.analyze_service.builder.FrontendResponseBuilder;
import com.modernbank.analyze_service.builder.InvoicePayloadBuilder;
import com.modernbank.analyze_service.client.AccountServiceClient;
import com.modernbank.analyze_service.client.InvoiceServiceClient;
import com.modernbank.analyze_service.client.TransactionServiceClient;
import com.modernbank.analyze_service.client.model.Account;
import com.modernbank.analyze_service.client.model.EnrichedTransaction;
import com.modernbank.analyze_service.client.model.InvoiceResponse;
import com.modernbank.analyze_service.client.model.TransactionAnalyzeModel;
import com.modernbank.analyze_service.entity.AnalysisReportEntity;
import com.modernbank.analyze_service.exception.ExternalServiceUnavailableException;
import com.modernbank.analyze_service.model.dto.*;
import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.service.AnalysisReportService;
import com.modernbank.analyze_service.service.MapperService;
import com.modernbank.analyze_service.service.impl.AnalysisReportServiceImpl;
import com.modernbank.analyze_service.service.analysis.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of analysis orchestration service.
 * Coordinates the entire analysis flow and Invoice Service call.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisOrchestrator implements AnalysisOrchestrationService {

    private final TransactionServiceClient transactionServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final InvoiceServiceClient invoiceServiceClient;

    private final AggregationService aggregationService;
    private final PatternDetectionService patternDetectionService;
    private final FraudCorrelationService fraudCorrelationService;
    private final RiskLevelCalculator riskLevelCalculator;

    private final FrontendResponseBuilder frontendResponseBuilder;
    private final InvoicePayloadBuilder invoicePayloadBuilder;
    private final AnalysisReportService analysisReportService;

    private final MapperService mapperService;

    @Override
    public AnalysisResult analyze(AnalyzeTransactionRequest request) {
        log.info("Starting analysis for user {} with range {}",
                request.getUserId(), request.getAnalyzeRange());

        // Step 1: Fetch transaction data (required)
        TransactionAnalyzeModel transactionData = fetchTransactionData(request);

        // Step 2: Fetch account data (optional, graceful degradation)
        List<Account> accounts = fetchAccountDataSafely(request);

        // Step 3: Extract transaction lists
        List<EnrichedTransaction> currentPeriod = transactionData.getCurrentPeriodTransactions();
        List<EnrichedTransaction> previousPeriod = transactionData.getPreviousPeriodTransactions();

        if (currentPeriod == null) {
            currentPeriod = Collections.emptyList();
        }
        if (previousPeriod == null) {
            previousPeriod = Collections.emptyList();
        }

        // Step 4: Run aggregation
        AggregationResult aggregation = aggregationService.aggregate(
                currentPeriod, request.getUserId());

        // Step 5: Detect patterns
        List<DetectedPattern> patterns = patternDetectionService.detectPatterns(
                currentPeriod,
                previousPeriod,
                request.getUserId(),
                transactionData.getHistoricalAverageAmount() != null
                        ? BigDecimal.valueOf(transactionData.getHistoricalAverageAmount())
                        : null,
                transactionData.getTotalHistoricalTransactionCount());

        // Step 6: Correlate fraud signals
        FraudCorrelationResult fraudResult = fraudCorrelationService.correlate(currentPeriod);

        // Step 7: Calculate risk level
        RiskLevel riskLevel = riskLevelCalculator.calculateRiskLevel(
                aggregation, patterns, fraudResult);

        // Step 8: Build frontend response
        var frontendResponse = frontendResponseBuilder.build(
                request.getAnalyzeRange(),
                riskLevel,
                aggregation,
                patterns,
                fraudResult);

        // Step 9: Save analysis report to database
        AnalysisReportEntity savedReport = null;
        try {
            savedReport = analysisReportService.save(frontendResponse, request.getUserId());
            log.info("Saved analysis report with ID: {} for user: {}",
                    savedReport.getId(), request.getUserId());
        } catch (Exception e) {
            log.error("Failed to save analysis report for user {}: {}",
                    request.getUserId(), e.getMessage());
            // Continue without failing - analysis result is still valid
        }

        // Step 10: Build invoice payload (include AI summary)
        DynamicInvoiceRequest invoicePayload = invoicePayloadBuilder.build(
                savedReport.getId() != null ? savedReport.getId() : "N/A",
                request.getUserId(),
                request.getAnalyzeRange(),
                riskLevel,
                aggregation,
                patterns,
                fraudResult,
                currentPeriod,
                accounts,
                frontendResponse.getAiSummary());

        // Step 11: Call Invoice Service
        InvoiceResponse invoiceResponse = callInvoiceServiceSafely(invoicePayload, request);

        log.info("Analysis complete for user {}: risk level = {}, {} patterns detected",
                request.getUserId(), riskLevel, patterns.size());

        return buildAnalysisResult(frontendResponse, invoiceResponse, savedReport);
    }

    private TransactionAnalyzeModel fetchTransactionData(AnalyzeTransactionRequest request) {
        try {
            log.debug("Fetching transaction data for user {}", request.getUserId());

            TransactionAnalyzeModel result = transactionServiceClient.getTransactionsForAnalysis(
                    mapperService.map(request, AnalyzeTransactionRequest.class)
            );

            if (result == null) {
                log.warn("Transaction service returned null for user {}", request.getUserId());
                return createEmptyTransactionModel();
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to fetch transaction data for user {}: {}",
                    request.getUserId(), e.getMessage());
            throw new ExternalServiceUnavailableException("transaction-service", e);
        }
    }

    private List<Account> fetchAccountDataSafely(AnalyzeTransactionRequest request) {
        try {
            log.debug("Fetching account data for user {}", request.getUserId());

            return mapperService.map(accountServiceClient.getAccountsByUserId(new BaseRequest())
                            .getAccounts(),
                    Account.class);
        } catch (Exception e) {
            log.warn("Failed to fetch account data for user {}, proceeding without: {}",
                    request.getUserId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private InvoiceResponse callInvoiceServiceSafely(
            DynamicInvoiceRequest invoicePayload,
            AnalyzeTransactionRequest request) {
        try {
            log.debug("Calling Invoice Service for user {}", request.getUserId());

            return invoiceServiceClient.generateInvoice(
                    invoicePayload,
                    request.getToken(),
                    request.getCorrelationId());

        } catch (Exception e) {
            log.warn("Failed to call Invoice Service for user {}: {}",
                    request.getUserId(), e.getMessage());
            return null;
        }
    }

    private AnalysisResult buildAnalysisResult(
            com.modernbank.analyze_service.api.response.AnalysisResponse frontendResponse,
            InvoiceResponse invoiceResponse,
            AnalysisReportEntity savedReport) {

        AnalysisResult.AnalysisResultBuilder builder = AnalysisResult.builder();
        // .frontendResponse(frontendResponse);

        if (savedReport != null) {
            builder.analysisReportId(savedReport.getId());
        }

        if (invoiceResponse != null) {
            builder.invoiceRequestId(invoiceResponse.getRequestId())
                    .invoiceStatus(invoiceResponse.getInvoiceStatus())
                    .estimatedCompletionDate(invoiceResponse.getEstimatedCompletionDate())
                    .invoiceMessage("Your analysis report will be ready by the estimated completion time.");
        } else {
            builder.invoiceMessage(
                    "Invoice generation is temporarily unavailable. Your analysis is still complete.");
        }

        return builder.build();
    }

    private TransactionAnalyzeModel createEmptyTransactionModel() {
        return TransactionAnalyzeModel.builder()
                .currentPeriodTransactions(Collections.emptyList())
                .previousPeriodTransactions(Collections.emptyList())
                .totalHistoricalTransactionCount(0)
                .historicalAverageAmount(0.0)
                .build();
    }
}
