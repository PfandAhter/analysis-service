package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.client.model.Account;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for Account Service.
 * Fetches account data for enrichment purposes (optional).
 */
@FeignClient(name = "account-service", url = "${service.account.url}")
public interface AccountServiceClient {

    /**
     * Fetches all accounts for a user.
     *
     * @param userId        User ID to fetch accounts for
     * @param token         Authorization token
     * @param correlationId Correlation ID for request tracing
     * @return List of accounts belonging to the user
     */
    @GetMapping("/api/v1/accounts")
    List<Account> getAccountsByUserId(
            @RequestParam("userId") String userId,
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Correlation-ID") String correlationId);
}
