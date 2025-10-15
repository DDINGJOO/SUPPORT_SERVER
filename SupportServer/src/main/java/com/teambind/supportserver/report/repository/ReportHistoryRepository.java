package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 신고 처리 이력 리포지토리
 */
@Repository
public interface ReportHistoryRepository extends JpaRepository<ReportHistory, String> {

}
