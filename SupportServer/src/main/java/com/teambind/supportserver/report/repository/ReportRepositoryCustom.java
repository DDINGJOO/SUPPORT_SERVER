package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.entity.Report;

import java.util.List;

/**
 * 신고 커스텀 리포지토리 인터페이스
 */
public interface ReportRepositoryCustom {

    /**
     * 커서 기반 페이징으로 신고 목록 조회
     *
     * @param searchRequest 검색 조건 (필터, 정렬, 커서)
     * @return 신고 목록
     */
    List<Report> findReportsWithCursor(ReportSearchRequest searchRequest);

    /**
     * 신고자 ID로 신고 목록 조회
     *
     * @param reporterId 신고자 ID
     * @return 신고 목록
     */
    List<Report> findByReporterId(String reporterId);

    /**
     * 신고 대상 ID로 신고 목록 조회
     *
     * @param reportedId 신고 대상 ID
     * @return 신고 목록
     */
    List<Report> findByReportedId(String reportedId);
}
