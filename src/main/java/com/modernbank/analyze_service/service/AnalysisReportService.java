package com.modernbank.analyze_service.service;

import com.modernbank.analyze_service.api.request.UpdateInvoiceIdRequest;
import com.modernbank.analyze_service.api.response.AnalysisResponse;
import com.modernbank.analyze_service.entity.AnalysisReportEntity;
import com.modernbank.analyze_service.model.dto.AnalysisReportModel;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportService {
    AnalysisReportEntity save(AnalysisResponse response, String userId);

    void updateInvoiceId(UpdateInvoiceIdRequest request);

    List<AnalysisReportModel> findByUserId(String accountId);

    Optional<AnalysisReportEntity> findById(String id);

    Optional<AnalysisReportEntity> findByInvoiceId(String invoiceId);
}
