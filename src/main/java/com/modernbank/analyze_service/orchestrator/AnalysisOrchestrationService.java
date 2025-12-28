package com.modernbank.analyze_service.orchestrator;

import com.modernbank.analyze_service.api.request.AnalyzeTransactionRequest;
import com.modernbank.analyze_service.model.dto.AnalysisResult;

/**
 * Interface for analysis orchestration service.
 * Follows SOLID - Interface Segregation Principle.
 */
public interface AnalysisOrchestrationService {

    /**
     * Orchestrates the complete analysis flow including:
     * - Fetching transaction and account data
     * - Running aggregation, pattern detection, and fraud correlation
     * - Calculating risk level
     * - Building frontend response and invoice payload
     * - Calling Invoice Service
     *
     * @param request The analysis request
     * @return Complete analysis result
     */
    AnalysisResult analyze(AnalyzeTransactionRequest request);
}
