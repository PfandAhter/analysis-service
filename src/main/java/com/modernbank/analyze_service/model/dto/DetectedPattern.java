package com.modernbank.analyze_service.model.dto;

import com.modernbank.analyze_service.model.enums.PatternSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a detected behavioral pattern in transaction analysis.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetectedPattern {

    /**
     * Unique pattern identifier (e.g., PAT_001).
     */
    private String patternId;

    /**
     * Human-readable pattern name.
     */
    private String patternName;

    /**
     * Severity level of this pattern.
     */
    private PatternSeverity severity;

    /**
     * Description explaining what was detected.
     */
    private String description;

    /**
     * List of transaction IDs affected by this pattern.
     */
    private List<String> affectedTransactionIds;

    /**
     * Count of affected transactions.
     */
    private int affectedCount;
}
