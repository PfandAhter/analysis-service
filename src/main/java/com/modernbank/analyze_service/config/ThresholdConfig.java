package com.modernbank.analyze_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configuration for analysis thresholds.
 * These are the default values used when user history is insufficient.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "analysis.threshold")
public class ThresholdConfig {

    /**
     * Minimum transaction count to use user's own historical average.
     * Below this, system defaults are used.
     */
    private int minimumHistoryForDynamic = 10;

    /**
     * Transaction count at which to fully trust user history.
     */
    private int fullHistoryThreshold = 20;

    /**
     * Default micro-transaction limit (in TRY).
     */
    private BigDecimal microTransactionLimit = new BigDecimal("50");

    /**
     * Multiplier for detecting large amount outliers.
     */
    private double largeTransactionMultiplier = 3.0;

    /**
     * Default average transaction amount if no history available.
     */
    private BigDecimal defaultAverageAmount = new BigDecimal("500");

    /**
     * Percentage threshold for velocity spike detection.
     */
    private double velocitySpikeThreshold = 0.5;

    /**
     * Percentage threshold for new receiver detection.
     */
    private double newReceiverThreshold = 0.3;

    /**
     * Count threshold for micro-transaction pattern.
     */
    private int microTransactionCountThreshold = 5;

    /**
     * Percentage threshold for off-hours activity.
     */
    private double offHoursThreshold = 0.4;

    /**
     * Percentage threshold for round amount clustering.
     */
    private double roundAmountThreshold = 0.5;
}
