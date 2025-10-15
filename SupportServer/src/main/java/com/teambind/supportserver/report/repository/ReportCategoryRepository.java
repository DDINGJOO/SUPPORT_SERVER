package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 신고 카테고리 리포지토리
 */
@Repository
public interface ReportCategoryRepository extends JpaRepository<ReportCategory, ReportCategoryId> {

}
