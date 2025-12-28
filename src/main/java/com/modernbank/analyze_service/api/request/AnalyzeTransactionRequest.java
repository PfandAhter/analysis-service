package com.modernbank.analyze_service.api.request;

import com.modernbank.analyze_service.model.enums.AnalyzeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeTransactionRequest extends BaseRequest {

    private AnalyzeRange analyzeRange;
}
