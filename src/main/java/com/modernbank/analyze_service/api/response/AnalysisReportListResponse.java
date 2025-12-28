package com.modernbank.analyze_service.api.response;

import com.modernbank.analyze_service.api.dto.AnalysisReportDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AnalysisReportListResponse  {
    List<AnalysisReportDTO> analysisReports;
}