package com.teambind.supportserver.report.controller;

import com.teambind.supportserver.report.dto.request.ReportRequest;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.dto.request.ReportStatusUpdateRequest;
import com.teambind.supportserver.report.dto.response.CursorPageResponse;
import com.teambind.supportserver.report.dto.response.ReportResponse;
import com.teambind.supportserver.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 신고 관리 컨트롤러
 *
 * <p>신고 등록, 조회, 검색, 상태 변경 API를 제공합니다.</p>
 * <p>프로필, 게시글, 비즈니스 등 모든 타입의 신고를 통합하여 처리합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 등록 (통합 API - 프로필, 게시글, 비즈니스 신고)
     *
     * @param request 신고 요청 정보 (reporterId, reportedId, referenceType, reportCategory, reason)
     * @return 생성된 신고 정보
     */
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody ReportRequest request) {
        log.info("Creating report: reporterId={}, reportedId={}, referenceType={}",
                request.getReporterId(), request.getReportedId(), request.getReferenceType());

        ReportResponse response = reportService.createReport(
                request.getReporterId(),
                request.getReportedId(),
                request.getReferenceType(),
                request.getReportCategory(),
                request.getReason()
        );

        log.info("Report created successfully: reportId={}", response.getReportId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 신고 상세 조회
     *
     * @param reportId 신고 ID
     * @return 신고 상세 정보
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable String reportId) {
        log.info("Fetching report: reportId={}", reportId);

        ReportResponse response = reportService.getReport(reportId);

        return ResponseEntity.ok(response);
    }

    /**
     * 신고 목록 검색 (커서 기반 페이징)
     *
     * @param request 검색 조건 (status, referenceType, reportCategory, sortType, sortDirection, cursor, size)
     * @return 신고 목록 (커서 페이징)
     */
    @GetMapping
    public ResponseEntity<CursorPageResponse<ReportResponse>> searchReports(
            @Valid @ModelAttribute ReportSearchRequest request) {
        log.info("Searching reports: status={}, referenceType={}, category={}, sortType={}, size={}",
                request.getStatus(), request.getReferenceType(), request.getReportCategory(),
                request.getSortType(), request.getSize());

        CursorPageResponse<ReportResponse> response = reportService.searchReports(request);

        log.info("Found {} reports, hasNext={}", response.getContent().size(), response.getHasNext());

        return ResponseEntity.ok(response);
    }

    /**
     * 신고 상태 변경 (통합 API - 승인, 거절, 검토 중, 보류)
     *
     * @param reportId 신고 ID
     * @param request  상태 변경 요청 정보 (status, adminId, comment)
     * @return 변경된 신고 정보
     */
    @PatchMapping("/{reportId}")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable String reportId,
            @Valid @RequestBody ReportStatusUpdateRequest request) {
        log.info("Updating report status: reportId={}, newStatus={}, adminId={}",
                reportId, request.getStatus(), request.getAdminId());

        reportService.updateReportStatus(
                reportId,
                request.getStatus(),
                request.getAdminId(),
                request.getComment()
        );

        ReportResponse response = reportService.getReport(reportId);

        log.info("Report status updated successfully: reportId={}, newStatus={}",
                reportId, response.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * 신고 철회 (신고자만 가능)
     *
     * @param reportId 신고 ID
     * @param request  철회 요청 정보 (reporterId, reason)
     * @return 철회된 신고 정보
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ReportResponse> withdrawReport(
            @PathVariable String reportId,
            @RequestBody WithdrawRequest request) {
        log.info("Withdrawing report: reportId={}, reporterId={}, reason={}", 
                reportId, request.getReporterId(), request.getReason());

        reportService.withdrawReport(reportId, request.getReporterId(), request.getReason());

        ReportResponse response = reportService.getReport(reportId);

        log.info("Report withdrawn successfully: reportId={}", reportId);

        return ResponseEntity.ok(response);
    }

    /**
     * 신고 철회 요청 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WithdrawRequest {
        private String reporterId;
        private String reason;
    }
}
