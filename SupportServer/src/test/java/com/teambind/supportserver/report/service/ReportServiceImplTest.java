package com.teambind.supportserver.report.service;

import com.teambind.supportserver.report.dto.request.ReportRequest;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.dto.response.CursorPageResponse;
import com.teambind.supportserver.report.dto.response.ReportResponse;
import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import com.teambind.supportserver.report.exceptions.ErrorCode;
import com.teambind.supportserver.report.exceptions.ReportException;
import com.teambind.supportserver.report.repository.ReportRepository;
import com.teambind.supportserver.report.utils.IdGenerator;
import com.teambind.supportserver.report.utils.ReportCategoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ReportServiceImpl 단위 테스트 (리팩토링 후)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl 단위 테스트")
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportCategoryCache reportCategoryCache;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private ReportServiceImpl reportService;

    private ReportCategory testCategory;
    private Report testReport;

    @BeforeEach
    void setUp() {
        testCategory = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.PROFILE, "SPAM"))
                .build();

        testReport = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트 신고")
                .reportedAt(LocalDateTime.now())
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();
        testReport.setCategory(testCategory);
    }

    @Test
    @DisplayName("신고 등록 - 성공")
    void createReport_Success() {
        // given
        ReportRequest request = ReportRequest.builder()
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("SPAM")
                .reason("테스트 신고")
                .build();

        given(reportCategoryCache.get(ReferenceType.PROFILE, "SPAM"))
                .willReturn(Optional.of(testCategory));
        given(idGenerator.generateId()).willReturn("123456789");
        given(reportRepository.save(any(Report.class))).willReturn(testReport);

        // when
        Report result = reportService.createReport(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReporterId()).isEqualTo("USER-001");
        assertThat(result.getReportedId()).isEqualTo("USER-002");
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 등록 - 카테고리 없음 예외")
    void createReport_CategoryNotFound() {
        // given
        ReportRequest request = ReportRequest.builder()
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("INVALID_CATEGORY")
                .reason("테스트 신고")
                .build();

        given(reportCategoryCache.get(ReferenceType.PROFILE, "INVALID_CATEGORY"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.createReport(request))
                .isInstanceOf(ReportException.class)
                .hasMessageContaining(ErrorCode.REPORT_CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("신고 상세 조회 - 성공")
    void getReportById_Success() {
        // given
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));

        // when
        ReportResponse result = reportService.getReportById("REPORT-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo("REPORT-001");
        assertThat(result.getReporterId()).isEqualTo("USER-001");
    }

    @Test
    @DisplayName("신고 상세 조회 - 없음 예외")
    void getReportById_NotFound() {
        // given
        given(reportRepository.findById("INVALID-ID")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.getReportById("INVALID-ID"))
                .isInstanceOf(ReportException.class)
                .hasMessageContaining(ErrorCode.REPORT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("신고 상태 변경 - 성공 (히스토리 자동 생성)")
    void updateReportStatus_Success() {
        // given
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));
        given(idGenerator.generateId()).willReturn("HISTORY-001");

        // when
        reportService.updateReportStatus("REPORT-001", ReportStatus.REVIEWING, "ADMIN-001", "검토 시작");

        // then
        assertThat(testReport.getStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(testReport.getHistories()).hasSize(1);
        assertThat(testReport.getHistories().get(0).getNewStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(testReport.getHistories().get(0).getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        verify(reportRepository, times(1)).findById("REPORT-001");
    }

    @Test
    @DisplayName("신고 승인 - 성공 (히스토리 자동 생성)")
    void approveReport_Success() {
        // given
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));
        given(idGenerator.generateId()).willReturn("HISTORY-001");

        // when
        reportService.approveReport("REPORT-001", "ADMIN-001");

        // then
        assertThat(testReport.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(testReport.getHistories()).hasSize(1);
        assertThat(testReport.getHistories().get(0).getAdminId()).isEqualTo("ADMIN-001");
    }

    @Test
    @DisplayName("신고 거부 - 성공 (히스토리 자동 생성)")
    void rejectReport_Success() {
        // given
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));
        given(idGenerator.generateId()).willReturn("HISTORY-001");

        // when
        reportService.rejectReport("REPORT-001", "ADMIN-001", "부적절한 신고");

        // then
        assertThat(testReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(testReport.getHistories()).hasSize(1);
        assertThat(testReport.getHistories().get(0).getComment()).isEqualTo("부적절한 신고");
    }

    @Test
    @DisplayName("신고 철회 - 성공 (히스토리 자동 생성)")
    void withdrawReport_Success() {
        // given
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));
        given(idGenerator.generateId()).willReturn("HISTORY-001");

        // when
        reportService.withdrawReport("REPORT-001", "USER-001");

        // then
        assertThat(testReport.getStatus()).isEqualTo(ReportStatus.WITHDRAWN);
        assertThat(testReport.getHistories()).hasSize(1);
    }

    @Test
    @DisplayName("신고 철회 - 본인 아님 예외")
    void withdrawReport_NotOwner() {
        // given
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));

        // when & then
        assertThatThrownBy(() -> reportService.withdrawReport("REPORT-001", "OTHER-USER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the reporter can withdraw this report");
    }

    @Test
    @DisplayName("신고 철회 - 대기 상태 아님 예외")
    void withdrawReport_NotPending() {
        // given
        given(idGenerator.generateId()).willReturn("HISTORY-001");
        testReport.approve("ADMIN-001", "승인", idGenerator::generateId); // APPROVED 상태로 변경
        given(reportRepository.findById("REPORT-001")).willReturn(Optional.of(testReport));

        // when & then
        assertThatThrownBy(() -> reportService.withdrawReport("REPORT-001", "USER-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending reports can be withdrawn");
    }

    @Test
    @DisplayName("커서 생성 - 신고일 기준")
    void generateCursor_ReportedAt() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .size(1)
                .build();

        List<Report> reports = List.of(testReport, testReport);
        given(reportRepository.findReportsWithCursor(request)).willReturn(reports);

        // when
        CursorPageResponse<ReportResponse> result = reportService.searchReports(request);

        // then
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextCursor()).contains("T"); // ISO 8601 format check
    }

    @Test
    @DisplayName("커서 생성 - 상태 기준")
    void generateCursor_Status() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .sortType(ReportSearchRequest.SortType.STATUS)
                .size(1)
                .build();

        List<Report> reports = List.of(testReport, testReport);
        given(reportRepository.findReportsWithCursor(request)).willReturn(reports);

        // when
        CursorPageResponse<ReportResponse> result = reportService.searchReports(request);

        // then
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextCursor()).isEqualTo("REPORT-001");
    }
}
