package com.modernbank.analyze_service.entity;

import com.modernbank.analyze_service.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for persisting analysis reports to the database.
 * Invoice Service will later update the invoiceId after generating an invoice.
 */
@Entity
@Table(name = "analysis_reports")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * The user/account ID for whom the analysis was generated.
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Invoice ID - will be set by Invoice Service after invoice generation.
     */
    @Column(name = "invoice_id")
    private String invoiceId;

    /**
     * Human-readable analysis range (e.g., "Son 7 Gün").
     */
    @Column(name = "analysis_range")
    private String analysisRange;

    /**
     * Overall risk level: LOW, MEDIUM, or HIGH.
     */
    @Column(name = "overall_risk_level", length = 20)
    private String overallRiskLevel;

    @Column(name = "invoice_status")
    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;

    /**
     * Brief summary of the analysis.
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * AI-generated Turkish language summary for user display.
     */
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    /**
     * Key findings stored as JSON string.
     */
    @Column(name = "key_findings", columnDefinition = "TEXT")
    private String keyFindings;

    /**
     * User guidance - what the user should be aware of.
     */
    @Column(name = "user_guidance", columnDefinition = "TEXT")
    private String userGuidance;

    /**
     * Flagged transaction IDs stored as JSON string.
     */
    @Column(name = "flagged_transaction_ids", columnDefinition = "TEXT")
    private String flaggedTransactionIds;

    /**
     * Timestamp when analysis was generated.
     */
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    /**
     * Total number of transactions analyzed.
     */
    @Column(name = "total_transactions")
    private Integer totalTransactions;

    /**
     * Total outgoing amount as formatted string.
     */
    @Column(name = "total_outgoing")
    private String totalOutgoing;

    /**
     * Total incoming amount as formatted string.
     */
    @Column(name = "total_incoming")
    private String totalIncoming;

    /**
     * Net flow as formatted string.
     */
    @Column(name = "net_flow")
    private String netFlow;

    /**
     * Timestamp when the record was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
