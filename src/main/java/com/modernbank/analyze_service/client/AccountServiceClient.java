package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.api.request.BaseRequest;
import com.modernbank.analyze_service.api.response.GetAccountsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for Account Service.
 * Fetches account data for enrichment purposes (optional).
 */
@FeignClient(name = "account-service", url = "${service.account.url}")
public interface AccountServiceClient {

    @PostMapping("/api/v1/account/getv2")
    GetAccountsResponse getAccountsByUserId(@RequestBody BaseRequest baseRequest);
}
