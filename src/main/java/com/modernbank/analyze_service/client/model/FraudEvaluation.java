package com.modernbank.analyze_service.client.model;

import com.modernbank.analyze_service.model.enums.RiskLevel;
import com.modernbank.analyze_service.model.enums.RecommendedAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fraud evaluation data received from Transaction Service.
 * This is advisory data only - Transaction is the source of truth.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudEvaluation {

    private String evaluationId;

    private String transactionId;

    /**
     * Risk score between 0.0 (no risk) and 1.0 (highest risk).
     */
    private Double riskScore;

    /**
     * Categorized risk level.
     */
    private RiskLevel riskLevel;

    /**
     * Recommended action based on fraud evaluation.
     */
    private RecommendedAction recommendedAction;

    /**
     * Raw feature vector JSON - internal use only, never expose to end user.
     */
    private String featureVector;

    /**
     * Feature importance JSON - used for fraud correlation analysis.
     * Format: {"feature_name": importance_score, ...}
     */
    private String featureImportance;
}
