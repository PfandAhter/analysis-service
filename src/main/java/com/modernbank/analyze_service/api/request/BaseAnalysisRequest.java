package com.modernbank.analyze_service.api.request;

import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Base class for analysis requests.
 * Contains common fields for all analysis operations.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseAnalysisRequest {

    private String userId;

    private AnalyzeRange analyzeRange;

    /**
     * Optional custom start date (only used when analyzeRange is not sufficient).
     */
    private LocalDateTime from;

    /**
     * Optional custom end date (only used when analyzeRange is not sufficient).
     */
    private LocalDateTime to;
}
