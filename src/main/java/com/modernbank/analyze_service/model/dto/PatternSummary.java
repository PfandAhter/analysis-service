package com.modernbank.analyze_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified pattern summary for invoice payload.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatternSummary {

    private String patternName;

    private String severity;

    private int affectedCount;

    public static PatternSummary fromDetectedPattern(DetectedPattern pattern) {
        return PatternSummary.builder()
                .patternName(pattern.getPatternName())
                .severity(pattern.getSeverity().name())
                .affectedCount(pattern.getAffectedCount())
                .build();
    }
}
