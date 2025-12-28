package com.modernbank.analyze_service.service.ai;

import com.modernbank.analyze_service.model.dto.AggregationResult;
import com.modernbank.analyze_service.model.dto.DetectedPattern;
import com.modernbank.analyze_service.model.dto.FraudCorrelationResult;
import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import com.modernbank.analyze_service.model.enums.RiskLevel;

import java.util.List;

public interface AiSummaryService {

    String generateTurkishSummary(
            AnalyzeRange analyzeRange,
            RiskLevel riskLevel,
            AggregationResult aggregation,
            List<DetectedPattern> patterns,
            FraudCorrelationResult fraudResult);
}