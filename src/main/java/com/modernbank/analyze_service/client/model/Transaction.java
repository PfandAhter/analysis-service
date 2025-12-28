package com.modernbank.analyze_service.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction data received from Transaction Service.
 * This is the single source of truth for transaction information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    private String transactionId;

    private String userId;

    private String senderAccountId;

    private String senderIban;

    private String receiverAccountId;

    private String receiverIban;

    private BigDecimal amount;

    private String currency;

    private String description;

    private String status;

    private String type;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
