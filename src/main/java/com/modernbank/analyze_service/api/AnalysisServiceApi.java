package com.modernbank.analyze_service.api;

import com.modernbank.analyze_service.api.request.AnalyzeTransactionRequest;
import com.modernbank.analyze_service.api.request.BaseRequest;
import com.modernbank.analyze_service.api.request.UpdateInvoiceIdRequest;
import com.modernbank.analyze_service.api.response.AnalysisReportListResponse;
import com.modernbank.analyze_service.api.response.BaseResponse;
import com.modernbank.analyze_service.model.dto.AnalysisResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AnalysisServiceApi {

    @PostMapping("/transactions")
    ResponseEntity<AnalysisResult> analyzeTransactions(@RequestBody AnalyzeTransactionRequest request);

    @PostMapping("/get/analysis")
    ResponseEntity<AnalysisReportListResponse> getAnalysisReports(@RequestBody BaseRequest baseRequest);

    @PostMapping("/reports/invoice/update")
    ResponseEntity<BaseResponse> updateInvoiceReports(@RequestBody UpdateInvoiceIdRequest request);

}
