package com.modernbank.analyze_service.client;

import com.modernbank.analyze_service.api.request.BaseRequest;
import com.modernbank.analyze_service.api.response.GetAccountsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.modernbank.analyze_service.constant.HeaderKey.*;

@FeignClient(name = "account-service", url = "${service.account.url}")
public interface AccountServiceClient {

    @PostMapping("/api/v1/account/getv2")
    GetAccountsResponse getAccountsByUserId(
            @RequestBody BaseRequest baseRequest,
            @RequestHeader(USER_ID) String userId,
            @RequestHeader(USER_ROLE) String role,
            @RequestHeader(AUTHORIZATION_TOKEN) String token
    );
}
