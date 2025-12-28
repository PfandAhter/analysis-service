package com.modernbank.analyze_service.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps technical fraud feature names to human-readable labels.
 * This prevents raw feature names from being exposed to end users.
 */
@Component
public class FeatureNameMapper {

    private final Map<String, String> featureMapping;

    public FeatureNameMapper() {
        this.featureMapping = new ConcurrentHashMap<>();
        initializeDefaultMappings();
    }

    private void initializeDefaultMappings() {
        // Amount-related features
        featureMapping.put("amount_zscore", "Unusual transaction amount");
        featureMapping.put("amount_deviation", "Amount differs from typical pattern");
        featureMapping.put("large_amount", "Transaction amount above normal range");
        featureMapping.put("small_amount", "Unusually small transaction amount");

        // Velocity features
        featureMapping.put("velocity_24h", "High transaction frequency");
        featureMapping.put("velocity_1h", "Multiple transactions in short period");
        featureMapping.put("transaction_count_spike", "Sudden increase in transaction volume");

        // Receiver/sender features
        featureMapping.put("receiver_first_seen", "New recipient account");
        featureMapping.put("new_receiver", "First transaction to this recipient");
        featureMapping.put("receiver_risk", "Recipient account has risk indicators");

        // Time-based features
        featureMapping.put("time_of_day_risk", "Transaction outside business hours");
        featureMapping.put("weekend_transaction", "Weekend transaction");
        featureMapping.put("late_night", "Late night transaction");

        // Location/geographic features
        featureMapping.put("country_risk", "Transaction to high-risk region");
        featureMapping.put("cross_border", "International transaction");

        // Pattern features
        featureMapping.put("round_amount", "Round transaction amount");
        featureMapping.put("repeated_amount", "Same amount repeated multiple times");
        featureMapping.put("split_transaction", "Potential transaction splitting");

        // Account features
        featureMapping.put("account_age", "Recent account activity change");
        featureMapping.put("dormant_account", "Previously inactive account");
    }

    /**
     * Maps a technical feature name to a human-readable label.
     *
     * @param technicalName The technical feature name
     * @return Human-readable label, or a sanitized version if not mapped
     */
    public String mapToHumanReadable(String technicalName) {
        if (technicalName == null) {
            return "Unknown factor";
        }

        return featureMapping.getOrDefault(
                technicalName.toLowerCase().trim(),
                sanitizeFeatureName(technicalName));
    }

    /**
     * Sanitizes a feature name that doesn't have a mapping.
     * Converts snake_case to readable format without exposing technical names.
     */
    private String sanitizeFeatureName(String featureName) {
        // Convert snake_case to readable format
        String sanitized = featureName
                .toLowerCase()
                .replace("_", " ")
                .replace("-", " ");

        // Capitalize first letter
        if (!sanitized.isEmpty()) {
            sanitized = Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1);
        }

        return sanitized + " factor";
    }

    /**
     * Adds a custom mapping.
     *
     * @param technicalName Technical feature name
     * @param humanReadable Human-readable label
     */
    public void addMapping(String technicalName, String humanReadable) {
        featureMapping.put(technicalName.toLowerCase().trim(), humanReadable);
    }
}
