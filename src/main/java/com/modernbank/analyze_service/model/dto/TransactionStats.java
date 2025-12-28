package com.modernbank.analyze_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStats {
    private int totalTransactions;
    private String totalOutgoing;
    private String totalIncoming;
    private String netFlow;
}