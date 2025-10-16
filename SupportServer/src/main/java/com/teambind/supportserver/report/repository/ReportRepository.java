package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 신고 리포지토리
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, String>, ReportRepositoryCustom {

}
