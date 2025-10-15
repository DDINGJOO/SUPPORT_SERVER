package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.ReportStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 신고 통계 집계 리포지토리
 */
@Repository
public interface ReportStatisticsRepository extends JpaRepository<ReportStatistics, String> {

}
