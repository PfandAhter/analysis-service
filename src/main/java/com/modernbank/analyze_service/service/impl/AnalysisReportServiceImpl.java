package com.modernbank.analyze_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernbank.analyze_service.api.request.UpdateInvoiceIdRequest;
import com.modernbank.analyze_service.api.response.AnalysisResponse;
import com.modernbank.analyze_service.entity.AnalysisReportEntity;
import com.modernbank.analyze_service.model.dto.AnalysisReportModel;
import com.modernbank.analyze_service.model.enums.InvoiceStatus;
import com.modernbank.analyze_service.repository.AnalysisReportRepository;
import com.modernbank.analyze_service.service.AnalysisReportService;
import com.modernbank.analyze_service.service.MapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisReportServiceImpl implements AnalysisReportService {

    private final AnalysisReportRepository analysisReportRepository;

    private final MapperService mapperService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @Override
    public AnalysisReportEntity save(AnalysisResponse response, String userId) {
        AnalysisReportEntity entity = mapToEntity(response, userId);
        AnalysisReportEntity savedEntity = analysisReportRepository.save(entity);
        log.info("Saved analysis report with ID: {} for user: {}", savedEntity.getId(), userId);
        return savedEntity;
    }

    @Transactional
    @Override
    public void updateInvoiceId(UpdateInvoiceIdRequest request) {
        AnalysisReportEntity report = findByInvoiceId(request.getAnalysisReportId()).orElseThrow(
                () -> new IllegalArgumentException("No analysis report found with invoice ID: " + request.getAnalysisReportId()) // TODO: Update this.
        );

        log.info("Updated invoice ID to {} for analysis report ID: {}", request.getInvoiceId(), request.getAnalysisReportId());

        report.setInvoiceId(request.getInvoiceId());
        report.setInvoiceStatus(request.getStatus());
        analysisReportRepository.save(report);
    }

    public Optional<AnalysisReportEntity> findById(String id) {
        return analysisReportRepository.findById(id);
    }

    @Override
    public List<AnalysisReportModel> findByUserId(String userId) {
        List<AnalysisReportEntity> reports = analysisReportRepository.findByUserId(userId);

        return mapperService.map(reports, AnalysisReportModel.class);
    }

    @Override
    public Optional<AnalysisReportEntity> findByInvoiceId(String invoiceId) {
        return analysisReportRepository.findByInvoiceId(invoiceId);
    }

    public Optional<AnalysisReportEntity> findLatestByUserId(String userId) {
        return analysisReportRepository.findTopByUserIdOrderByGeneratedAtDesc(userId);
    }

    private AnalysisReportEntity mapToEntity(AnalysisResponse response, String userId) {
        return AnalysisReportEntity.builder()
                .userId(userId)
                .analysisRange(response.getAnalysisRange())
                .overallRiskLevel(response.getOverallRiskLevel())
                .invoiceStatus(InvoiceStatus.PENDING)
                .summary(response.getSummary())
                .aiSummary(response.getAiSummary())
                .keyFindings(toJson(response.getKeyFindings()))
                .userGuidance(response.getUserGuidance())
                .flaggedTransactionIds(toJson(response.getFlaggedTransactionIds()))
                .generatedAt(response.getGeneratedAt())
                .totalTransactions(response.getTotalTransactions())
                .totalOutgoing(response.getTotalOutgoing())
                .totalIncoming(response.getTotalIncoming())
                .netFlow(response.getNetFlow())
                .build();
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON", e);
            return "[]";
        }
    }
}
