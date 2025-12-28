package com.modernbank.analyze_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Summary of an account for reporting purposes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummary {

    private String accountId;

    private String iban;

    /**
     * Number of transactions involving this account.
     */
    private int transactionCount;

    /**
     * Net flow for this account (incoming - outgoing).
     */
    private BigDecimal netFlow;

    /**
     * Total outgoing from this account.
     */
    private BigDecimal outgoingAmount;

    /**
     * Total incoming to this account.
     */
    private BigDecimal incomingAmount;
}
