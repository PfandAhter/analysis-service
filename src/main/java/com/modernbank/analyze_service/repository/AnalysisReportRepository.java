package com.modernbank.analyze_service.repository;

import com.modernbank.analyze_service.entity.AnalysisReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReportEntity, String> {

    List<AnalysisReportEntity> findByUserId(String userId);

    Optional<AnalysisReportEntity> findByInvoiceId(String invoiceId);

    Optional<AnalysisReportEntity> findTopByUserIdOrderByGeneratedAtDesc(String userId);
}
