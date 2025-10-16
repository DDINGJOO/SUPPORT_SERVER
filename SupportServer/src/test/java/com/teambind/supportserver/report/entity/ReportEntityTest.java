package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Report 엔티티 단위 테스트 (리팩토링 후)
 */
@DisplayName("Report 엔티티 테스트")
class ReportEntityTest {

    private final AtomicInteger historyIdCounter = new AtomicInteger(1);

    private String generateHistoryId() {
        return "HISTORY-" + historyIdCounter.getAndIncrement();
    }

    @Test
    @DisplayName("Report 엔티티 생성 - 정상")
    void createReport_Success() {
        // given
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.PROFILE, "SPAM"))
                .build();

        // when
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("스팸 계정입니다")
                .reportedAt(LocalDateTime.now())
                .histories(new ArrayList<>())
                .build();

        report.setCategory(category);

        // then
        assertThat(report).isNotNull();
        assertThat(report.getReportId()).isEqualTo("REPORT-001");
        assertThat(report.getReporterId()).isEqualTo("USER-001");
        assertThat(report.getReportedId()).isEqualTo("USER-002");
        assertThat(report.getReason()).isEqualTo("스팸 계정입니다");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getCategory()).isEqualTo(category);
        assertThat(report.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(report.getReportCategory()).isEqualTo("SPAM");
        assertThat(report.getHistories()).isEmpty();
    }

    @Test
    @DisplayName("Report 엔티티 생성 - 기본 상태는 PENDING")
    void createReport_DefaultStatus() {
        // when
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("부적절한 콘텐츠")
                .histories(new ArrayList<>())
                .build();

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("신고 승인 - 상태가 APPROVED로 변경되고 히스토리 생성")
    void approveReport_StatusChangedAndHistoryCreated() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("욕설 사용")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.approve("ADMIN-001", "신고 승인", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(report.getHistories()).hasSize(1);
        assertThat(report.getHistories().get(0).getNewStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(report.getHistories().get(0).getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getHistories().get(0).getAdminId()).isEqualTo("ADMIN-001");
        assertThat(report.getHistories().get(0).getComment()).isEqualTo("신고 승인");
    }

    @Test
    @DisplayName("신고 거부 - 상태가 REJECTED로 변경되고 히스토리 생성")
    void rejectReport_StatusChangedAndHistoryCreated() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("허위 신고")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.reject("ADMIN-001", "근거 부족", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(report.getHistories()).hasSize(1);
        assertThat(report.getHistories().get(0).getNewStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(report.getHistories().get(0).getComment()).isEqualTo("근거 부족");
    }

    @Test
    @DisplayName("신고 철회 - 본인이 철회하면 성공하고 히스토리 생성")
    void withdrawReport_ByOwner_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("실수로 신고함")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.withdraw("USER-001", "실수로 신고", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.WITHDRAWN);
        assertThat(report.getHistories()).hasSize(1);
        assertThat(report.getHistories().get(0).getNewStatus()).isEqualTo(ReportStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("신고 철회 - 본인이 아니면 예외 발생")
    void withdrawReport_ByNonOwner_ThrowsException() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("실수로 신고함")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when & then
        assertThatThrownBy(() -> report.withdraw("OTHER-USER", "철회", this::generateHistoryId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the reporter can withdraw this report");
    }

    @Test
    @DisplayName("신고 철회 - PENDING 상태가 아니면 예외 발생")
    void withdrawReport_WhenNotPending_ThrowsException() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("검토 중")
                .status(ReportStatus.REVIEWING)
                .histories(new ArrayList<>())
                .build();

        // when & then
        assertThatThrownBy(() -> report.withdraw("USER-001", "철회", this::generateHistoryId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending reports can be withdrawn");
    }

    @Test
    @DisplayName("검토 시작 - 상태가 REVIEWING으로 변경되고 히스토리 생성")
    void startReview_StatusChangedAndHistoryCreated() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("검토 필요")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.startReview("ADMIN-001", "검토 시작", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(report.getHistories()).hasSize(1);
    }

    @Test
    @DisplayName("상태 변경 - 동일한 상태로 변경 시 히스토리 생성하지 않음")
    void changeStatus_SameStatus_NoHistoryCreated() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.changeStatus(ReportStatus.PENDING, "ADMIN-001", "동일 상태", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getHistories()).isEmpty();
    }

    @Test
    @DisplayName("상태 변경 - 여러 번 변경 시 히스토리 누적")
    void changeStatus_MultipleChanges_HistoriesAccumulated() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.startReview("ADMIN-001", "검토 시작", this::generateHistoryId);
        report.approve("ADMIN-001", "승인", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(report.getHistories()).hasSize(2);
        assertThat(report.getHistories().get(0).getNewStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(report.getHistories().get(1).getNewStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("isPending - PENDING 상태일 때 true 반환")
    void isPending_WhenStatusIsPending_ReturnsTrue() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("대기 중")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when & then
        assertThat(report.isPending()).isTrue();
    }

    @Test
    @DisplayName("isPending - PENDING 상태가 아닐 때 false 반환")
    void isPending_WhenStatusIsNotPending_ReturnsFalse() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("검토 중")
                .status(ReportStatus.REVIEWING)
                .histories(new ArrayList<>())
                .build();

        // when & then
        assertThat(report.isPending()).isFalse();
    }

    @Test
    @DisplayName("isApproved - APPROVED 상태일 때 true 반환")
    void isApproved_WhenStatusIsApproved_ReturnsTrue() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("승인됨")
                .status(ReportStatus.APPROVED)
                .histories(new ArrayList<>())
                .build();

        // when & then
        assertThat(report.isApproved()).isTrue();
    }

    @Test
    @DisplayName("changeStatus - 상태 변경 성공하고 히스토리 생성")
    void changeStatus_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("상태 변경 테스트")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.changeStatus(ReportStatus.REVIEWING, "ADMIN-001", "검토 중", this::generateHistoryId);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(report.getHistories()).hasSize(1);
    }

    @Test
    @DisplayName("setCategory - 카테고리 설정 시 referenceType과 reportCategory 자동 설정")
    void setCategory_AutoSetReferenceTypeAndReportCategory() {
        // given
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.ARTICLE, "INAPPROPRIATE"))
                .build();

        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("부적절한 게시글")
                .histories(new ArrayList<>())
                .build();

        // when
        report.setCategory(category);

        // then
        assertThat(report.getCategory()).isEqualTo(category);
        assertThat(report.getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(report.getReportCategory()).isEqualTo("INAPPROPRIATE");
    }

    @Test
    @DisplayName("setCategory - null 카테고리 설정 시 referenceType과 reportCategory는 null")
    void setCategory_WithNullCategory_NoAutoSet() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("카테고리 없음")
                .histories(new ArrayList<>())
                .build();

        // when
        report.setCategory(null);

        // then
        assertThat(report.getCategory()).isNull();
        assertThat(report.getReferenceType()).isNull();
        assertThat(report.getReportCategory()).isNull();
    }

    @Test
    @DisplayName("히스토리 - Report와 양방향 연관관계 설정 확인")
    void history_BidirectionalRelationship() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트")
                .status(ReportStatus.PENDING)
                .histories(new ArrayList<>())
                .build();

        // when
        report.approve("ADMIN-001", "승인", this::generateHistoryId);

        // then
        ReportHistory history = report.getHistories().get(0);
        assertThat(history.getReport()).isEqualTo(report);
        assertThat(history.getReport().getReportId()).isEqualTo("REPORT-001");
    }
}
