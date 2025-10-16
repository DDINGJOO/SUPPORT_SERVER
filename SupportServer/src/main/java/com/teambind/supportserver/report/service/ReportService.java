package com.teambind.supportserver.report.service;

import com.teambind.supportserver.report.dto.request.ReportRequest;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.dto.response.CursorPageResponse;
import com.teambind.supportserver.report.dto.response.ReportResponse;
import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.enums.ReportStatus;

import java.util.List;

/**
 * 신고 서비스 인터페이스
 */
public interface ReportService {

    /**
     * 신고 등록 (개별 파라미터)
     */
    ReportResponse createReport(String reporterId, String reportedId, com.teambind.supportserver.report.entity.enums.ReferenceType referenceType, String reportCategory, String reason);

    /**
     * 신고 등록 (DTO)
     *
     * @param request 신고 요청 DTO
     * @return 등록된 신고 엔티티
     */
    Report createReport(ReportRequest request);

    /**
     * 신고 상세 조회
     *
     * @param reportId 신고 ID
     * @return 신고 응답 DTO
     */
    ReportResponse getReport(String reportId);

    /**
     * 신고 상세 조회
     *
     * @param reportId 신고 ID
     * @return 신고 응답 DTO
     */
    ReportResponse getReportById(String reportId);

    /**
     * 신고 목록 조회 (커서 기반 페이징, 필터링, 정렬)
     *
     * @param searchRequest 검색 조건 (필터, 정렬, 커서)
     * @return 커서 기반 페이징 응답
     */
    CursorPageResponse<ReportResponse> searchReports(ReportSearchRequest searchRequest);

    /**
     * 특정 사용자가 신고한 내역 조회
     *
     * @param reporterId 신고자 ID
     * @return 신고 목록
     */
    List<ReportResponse> getReportsByReporter(String reporterId);

    /**
     * 특정 대상에 대한 신고 내역 조회
     *
     * @param reportedId 신고 대상 ID
     * @return 신고 목록
     */
    List<ReportResponse> getReportsByReportedId(String reportedId);

    /**
     * 신고 상태 변경 (히스토리 자동 생성)
     *
     * @param reportId  신고 ID
     * @param newStatus 변경할 상태
     * @param adminId   처리한 관리자 ID
     * @param comment   변경 사유/코멘트
     */
    void updateReportStatus(String reportId, ReportStatus newStatus, String adminId, String comment);

    /**
     * 신고 승인 및 제재 적용
     *
     * @param reportId 신고 ID
     * @param adminId  처리한 관리자 ID
     */
    void approveReport(String reportId, String adminId);

    /**
     * 신고 거부 (기각)
     *
     * @param reportId 신고 ID
     * @param adminId  처리한 관리자 ID
     * @param reason   거부 사유
     */
    void rejectReport(String reportId, String adminId, String reason);

    /**
     * 신고 철회
     *
     * @param reportId   신고 ID
     * @param reporterId 신고자 ID (본인 확인용)
     * @param reason     철회 사유
     */
    void withdrawReport(String reportId, String reporterId, String reason);

    /**
     * 신고 철회
     *
     * @param reportId   신고 ID
     * @param reporterId 신고자 ID (본인 확인용)
     */
    void withdrawReport(String reportId, String reporterId);

    /**
     * 신고 검토 시작
     *
     * @param reportId 신고 ID
     * @param adminId  처리한 관리자 ID
     */
    void startReview(String reportId, String adminId);

    /**
     * 신고 보류
     *
     * @param reportId 신고 ID
     * @param adminId  처리한 관리자 ID
     * @param reason   보류 사유
     */
    void holdReport(String reportId, String adminId, String reason);
}
