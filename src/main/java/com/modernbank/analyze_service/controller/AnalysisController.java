package com.modernbank.analyze_service.controller;

import com.modernbank.analyze_service.api.AnalysisServiceApi;
import com.modernbank.analyze_service.api.dto.AnalysisReportDTO;
import com.modernbank.analyze_service.api.request.AnalyzeTransactionRequest;
import com.modernbank.analyze_service.api.request.BaseRequest;
import com.modernbank.analyze_service.api.request.UpdateInvoiceIdRequest;
import com.modernbank.analyze_service.api.response.AnalysisReportListResponse;
import com.modernbank.analyze_service.api.response.BaseResponse;
import com.modernbank.analyze_service.entity.AnalysisReportEntity;
import com.modernbank.analyze_service.model.dto.AnalysisReportModel;
import com.modernbank.analyze_service.model.dto.AnalysisResult;
import com.modernbank.analyze_service.orchestrator.AnalysisOrchestrationService;
import com.modernbank.analyze_service.service.AnalysisReportService;
import com.modernbank.analyze_service.service.MapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController implements AnalysisServiceApi {

    private final AnalysisOrchestrationService analysisOrchestrationService;
    private final AnalysisReportService analysisReportService;

    private final MapperService mapperService;

    @Override
    public ResponseEntity<AnalysisResult> analyzeTransactions(AnalyzeTransactionRequest request) {
        log.info("Received analysis request for user {} with range {}",
                request.getUserId(), request.getAnalyzeRange());
        return ResponseEntity.ok(analysisOrchestrationService.analyze(request));
    }

    @Override
    public ResponseEntity<AnalysisReportListResponse> getAnalysisReports(BaseRequest baseRequest) {
        List<AnalysisReportModel> analysisReportsModel = analysisReportService
                .findByUserId(baseRequest.getUserId());

        return ResponseEntity.ok(new AnalysisReportListResponse(mapperService.map(analysisReportsModel, AnalysisReportDTO.class)));
    }

    @Override
    public ResponseEntity<BaseResponse> updateInvoiceReports(UpdateInvoiceIdRequest request) {
        analysisReportService.updateInvoiceId(request);
        return ResponseEntity.ok(new BaseResponse("Invoice ID updated successfully."));
    }
}