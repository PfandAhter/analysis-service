package com.modernbank.analyze_service.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Account data received from Account Service for enrichment purposes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private String accountId;

    private String userId;

    private String iban;

    private String accountType;

    private BigDecimal balance;

    private String currency;

    private String status;

    private String accountHolderName;
}
