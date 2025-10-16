package com.teambind.supportserver.report.service;

import com.teambind.supportserver.report.dto.request.ReportRequest;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.dto.response.CursorPageResponse;
import com.teambind.supportserver.report.dto.response.ReportResponse;
import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import com.teambind.supportserver.report.exceptions.ErrorCode;
import com.teambind.supportserver.report.exceptions.ReportException;
import com.teambind.supportserver.report.repository.ReportRepository;
import com.teambind.supportserver.report.utils.IdGenerator;
import com.teambind.supportserver.report.utils.ReportCategoryCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 신고 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportCategoryCache reportCategoryCache;
    private final IdGenerator idGenerator;


    @Override
    @Transactional
    public Report createReport(ReportRequest request) {
        log.info("Creating report: reporterId={}, reportedId={}, referenceType={}, category={}",
                request.getReporterId(), request.getReportedId(),
                request.getReferenceType(), request.getReportCategory());

        // 1. 카테고리 존재 여부 검증 (캐시 조회)
	    ReportCategory category =validateCategory(request.getReferenceType(), request.getReportCategory());

        // 2. Report ID 생성
        String reportId = String.valueOf(idGenerator.generateId());

        // 3. Report 엔티티 생성
        Report report = Report.builder()
                .reportId(reportId)
                .reporterId(request.getReporterId())
                .reportedId(request.getReportedId())
                .reason(request.getReason())
                .build();

        // 4. 카테고리 설정 (연관관계 편의 메서드)
        report.setCategory(category);

        // 5. 저장
        Report savedReport = reportRepository.save(report);

        log.info("Report created successfully: reportId={}", reportId);

        return savedReport;
    }

    @Override
    public ReportResponse getReportById(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ErrorCode.REPORT_NOT_FOUND));
        return ReportResponse.from(report);
    }

    @Override
    public CursorPageResponse<ReportResponse> searchReports(ReportSearchRequest searchRequest) {
        log.info("Searching reports with filters: status={}, referenceType={}, reportCategory={}, sortType={}, sortDirection={}",
                searchRequest.getStatus(), searchRequest.getReferenceType(),
                searchRequest.getReportCategory(), searchRequest.getSortType(), searchRequest.getSortDirection());

        // Repository에서 size + 1개 조회 (다음 페이지 존재 여부 확인용)
        List<Report> reports = reportRepository.findReportsWithCursor(searchRequest);

        // 실제 반환할 데이터는 size만큼
        int requestedSize = searchRequest.getSize();
        boolean hasNext = reports.size() > requestedSize;

        List<ReportResponse> content = reports.stream()
                .limit(requestedSize)
                .map(ReportResponse::from)
                .collect(Collectors.toList());

        // 다음 커서 생성
        String nextCursor = null;
        if (hasNext && !content.isEmpty()) {
            Report lastReport = reports.get(requestedSize - 1);
            nextCursor = generateCursor(lastReport, searchRequest.getSortType());
        }

        log.info("Found {} reports, hasNext={}", content.size(), hasNext);

        return hasNext
                ? CursorPageResponse.of(content, nextCursor, requestedSize)
                : CursorPageResponse.last(content, requestedSize);
    }

    @Override
    public List<ReportResponse> getReportsByReporter(String reporterId) {
        log.info("Fetching reports by reporter: reporterId={}", reporterId);
        List<Report> reports = reportRepository.findByReporterId(reporterId);
        return reports.stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportResponse> getReportsByReportedId(String reportedId) {
        log.info("Fetching reports by reported: reportedId={}", reportedId);
        List<Report> reports = reportRepository.findByReportedId(reportedId);
        return reports.stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateReportStatus(String reportId, ReportStatus newStatus, String adminId, String comment) {
        Report report = findReportEntity(reportId);
        report.changeStatus(newStatus, adminId, comment, idGenerator::generateId);

        log.info("Report status updated: reportId={}, newStatus={}, adminId={}", reportId, newStatus, adminId);
    }

    @Override
    @Transactional
    public void approveReport(String reportId, String adminId) {
        Report report = findReportEntity(reportId);
        report.approve(adminId, "신고 승인 - 제재 적용", idGenerator::generateId);

        log.info("Report approved: reportId={}, adminId={}", reportId, adminId);

        // TODO: 제재 로직 추가 (SanctionService 연동)
    }

    @Override
    @Transactional
    public void rejectReport(String reportId, String adminId, String reason) {
        Report report = findReportEntity(reportId);
        report.reject(adminId, reason, idGenerator::generateId);

        log.info("Report rejected: reportId={}, adminId={}, reason={}", reportId, adminId, reason);
    }

    @Override
    @Transactional
    public void withdrawReport(String reportId, String reporterId, String reason) {
        Report report = findReportEntity(reportId);
        report.withdraw(reporterId, reason != null ? reason : "신고자가 직접 철회", idGenerator::generateId);

        log.info("Report withdrawn: reportId={}, reporterId={}, reason={}", reportId, reporterId, reason);
    }

    @Override
    @Transactional
    public void withdrawReport(String reportId, String reporterId) {
        withdrawReport(reportId, reporterId, "신고자가 직접 철회");
    }

    @Override
    @Transactional
    public void startReview(String reportId, String adminId) {
        Report report = findReportEntity(reportId);
        report.startReview(adminId, "검토 시작", idGenerator::generateId);

        log.info("Review started: reportId={}, adminId={}", reportId, adminId);
    }

    @Override
    @Transactional
    public void holdReport(String reportId, String adminId, String reason) {
        Report report = findReportEntity(reportId);
        report.hold(adminId, reason != null ? reason : "보류 처리", idGenerator::generateId);

        log.info("Report held: reportId={}, adminId={}, reason={}", reportId, adminId, reason);
    }

    @Override
    public ReportResponse createReport(String reporterId, String reportedId, ReferenceType referenceType, String reportCategory, String reason) {
        ReportRequest request = ReportRequest.builder()
                .reporterId(reporterId)
                .reportedId(reportedId)
                .referenceType(referenceType)
                .reportCategory(reportCategory)
                .reason(reason)
                .build();

        Report report = createReport(request);
        return ReportResponse.from(report);
    }

    @Override
    public ReportResponse getReport(String reportId) {
        return getReportById(reportId);
    }

    /**
     * Report 엔티티 조회 (내부용)
     */
    private Report findReportEntity(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ErrorCode.REPORT_NOT_FOUND));
    }
	
	private ReportCategory validateCategory(ReferenceType referenceType, String reportCategory) {
		return reportCategoryCache
				.get(referenceType, reportCategory)
				.orElseThrow(() -> {
					log.warn("Report category not found: referenceType={}, category={}",
							referenceType,reportCategory);
					return new ReportException(ErrorCode.REPORT_CATEGORY_NOT_FOUND);
				});
	}

    /**
     * 커서 생성 (정렬 기준에 따라)
     */
    private String generateCursor(Report report, ReportSearchRequest.SortType sortType) {
        if (sortType == ReportSearchRequest.SortType.REPORTED_AT) {
            // 신고일 기준: ISO 8601 형식의 LocalDateTime
            return report.getReportedAt().toString();
        } else {
            // 상태 기준: reportId를 커서로 사용
            return report.getReportId();
        }
    }
}
