package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.api.request.AnalyzeTransactionRequest;
import com.modernbank.analyze_service.client.model.TransactionAnalyzeModel;
import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.modernbank.analyze_service.constant.HeaderKey.*;


@FeignClient(name = "transaction-service", url = "${service.transaction.url}")
public interface TransactionServiceClient {

    @PostMapping("/api/v1/transactions/analyze")
    TransactionAnalyzeModel getTransactionsForAnalysis(
            @RequestBody AnalyzeTransactionRequest request,
            @RequestHeader(USER_ID) String userId,
            @RequestHeader(USER_ROLE) String role,
            @RequestHeader(AUTHORIZATION_TOKEN) String token
    );
}
