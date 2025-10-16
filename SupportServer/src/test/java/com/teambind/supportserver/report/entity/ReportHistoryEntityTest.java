package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ActionType;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportHistory 엔티티 단위 테스트
 */
@DisplayName("ReportHistory 엔티티 테스트")
class ReportHistoryEntityTest {

    @Test
    @DisplayName("ReportHistory 엔티티 생성 - 정상")
    void createReportHistory_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reason("스팸")
                .status(ReportStatus.PENDING)
                .build();

        // when
        ReportHistory history = ReportHistory.builder()
                .historyId("HISTORY-001")
                .report(report)
                .adminId("ADMIN-001")
                .previousStatus(ReportStatus.PENDING)
                .newStatus(ReportStatus.REVIEWING)
                .actionType(ActionType.STATUS_CHANGED)
                .comment("검토 시작")
                .build();

        // then
        assertThat(history).isNotNull();
        assertThat(history.getHistoryId()).isEqualTo("HISTORY-001");
        assertThat(history.getReport()).isEqualTo(report);
        assertThat(history.getAdminId()).isEqualTo("ADMIN-001");
        assertThat(history.getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(history.getNewStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(history.getActionType()).isEqualTo(ActionType.STATUS_CHANGED);
        assertThat(history.getComment()).isEqualTo("검토 시작");
    }

    @Test
    @DisplayName("createStatusChangeHistory - 상태 변경 이력 생성")
    void createStatusChangeHistory_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .status(ReportStatus.PENDING)
                .build();

        // when
        ReportHistory history = ReportHistory.createStatusChangeHistory(
                "HISTORY-001",
                report,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.APPROVED,
                "신고 승인"
        );

        // then
        assertThat(history).isNotNull();
        assertThat(history.getActionType()).isEqualTo(ActionType.STATUS_CHANGED);
        assertThat(history.getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(history.getNewStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(history.getComment()).isEqualTo("신고 승인");
    }

    @Test
    @DisplayName("createReviewedHistory - 검토 완료 이력 생성")
    void createReviewedHistory_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .status(ReportStatus.REVIEWING)
                .build();

        // when
        ReportHistory history = ReportHistory.createReviewedHistory(
                "HISTORY-002",
                report,
                "ADMIN-002",
                "검토 완료되었습니다"
        );

        // then
        assertThat(history).isNotNull();
        assertThat(history.getActionType()).isEqualTo(ActionType.REVIEWED);
        assertThat(history.getAdminId()).isEqualTo("ADMIN-002");
        assertThat(history.getComment()).isEqualTo("검토 완료되었습니다");
    }

    @Test
    @DisplayName("createSanctionAppliedHistory - 제재 적용 이력 생성")
    void createSanctionAppliedHistory_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .status(ReportStatus.APPROVED)
                .build();

        // when
        ReportHistory history = ReportHistory.createSanctionAppliedHistory(
                "HISTORY-003",
                report,
                "ADMIN-003",
                "7일 정지 제재 적용"
        );

        // then
        assertThat(history).isNotNull();
        assertThat(history.getActionType()).isEqualTo(ActionType.SANCTION_APPLIED);
        assertThat(history.getAdminId()).isEqualTo("ADMIN-003");
        assertThat(history.getComment()).isEqualTo("7일 정지 제재 적용");
    }

    @Test
    @DisplayName("createAssignedHistory - 담당자 할당 이력 생성")
    void createAssignedHistory_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .status(ReportStatus.PENDING)
                .build();

        // when
        ReportHistory history = ReportHistory.createAssignedHistory(
                "HISTORY-004",
                report,
                "ADMIN-004",
                "담당자 할당됨"
        );

        // then
        assertThat(history).isNotNull();
        assertThat(history.getActionType()).isEqualTo(ActionType.ASSIGNED);
        assertThat(history.getAdminId()).isEqualTo("ADMIN-004");
        assertThat(history.getComment()).isEqualTo("담당자 할당됨");
    }

    @Test
    @DisplayName("createCommentAddedHistory - 코멘트 추가 이력 생성")
    void createCommentAddedHistory_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .status(ReportStatus.REVIEWING)
                .build();

        // when
        ReportHistory history = ReportHistory.createCommentAddedHistory(
                "HISTORY-005",
                report,
                "ADMIN-005",
                "추가 검토가 필요합니다"
        );

        // then
        assertThat(history).isNotNull();
        assertThat(history.getActionType()).isEqualTo(ActionType.COMMENT_ADDED);
        assertThat(history.getAdminId()).isEqualTo("ADMIN-005");
        assertThat(history.getComment()).isEqualTo("추가 검토가 필요합니다");
    }

    @Test
    @DisplayName("ReportHistory - 여러 액션 타입 테스트")
    void reportHistory_DifferentActionTypes() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .build();

        // when
        ReportHistory statusChange = ReportHistory.builder()
                .historyId("H-001")
                .report(report)
                .actionType(ActionType.STATUS_CHANGED)
                .newStatus(ReportStatus.REVIEWING)
                .build();

        ReportHistory reviewed = ReportHistory.builder()
                .historyId("H-002")
                .report(report)
                .actionType(ActionType.REVIEWED)
                .build();

        ReportHistory sanctionApplied = ReportHistory.builder()
                .historyId("H-003")
                .report(report)
                .actionType(ActionType.SANCTION_APPLIED)
                .newStatus(ReportStatus.APPROVED)
                .build();

        ReportHistory assigned = ReportHistory.builder()
                .historyId("H-004")
                .report(report)
                .actionType(ActionType.ASSIGNED)
                .build();

        ReportHistory commentAdded = ReportHistory.builder()
                .historyId("H-005")
                .report(report)
                .actionType(ActionType.COMMENT_ADDED)
                .build();

        // then
        assertThat(statusChange.getActionType()).isEqualTo(ActionType.STATUS_CHANGED);
        assertThat(reviewed.getActionType()).isEqualTo(ActionType.REVIEWED);
        assertThat(sanctionApplied.getActionType()).isEqualTo(ActionType.SANCTION_APPLIED);
        assertThat(assigned.getActionType()).isEqualTo(ActionType.ASSIGNED);
        assertThat(commentAdded.getActionType()).isEqualTo(ActionType.COMMENT_ADDED);
    }

    @Test
    @DisplayName("ReportHistory - 상태 변경 추적 시나리오")
    void reportHistory_StatusChangeTrackingScenario() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .status(ReportStatus.PENDING)
                .build();

        // when - PENDING -> REVIEWING
        ReportHistory history1 = ReportHistory.createStatusChangeHistory(
                "H-001",
                report,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.REVIEWING,
                "검토 시작"
        );

        // when - REVIEWING -> APPROVED
        ReportHistory history2 = ReportHistory.createStatusChangeHistory(
                "H-002",
                report,
                "ADMIN-001",
                ReportStatus.REVIEWING,
                ReportStatus.APPROVED,
                "신고 승인"
        );

        // then
        assertThat(history1.getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(history1.getNewStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(history2.getPreviousStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(history2.getNewStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("ReportHistory - adminId 없이 생성 (시스템 자동 처리)")
    void reportHistory_WithoutAdminId() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .build();

        // when
        ReportHistory history = ReportHistory.builder()
                .historyId("H-001")
                .report(report)
                .adminId(null)
                .actionType(ActionType.STATUS_CHANGED)
                .previousStatus(null)
                .newStatus(ReportStatus.PENDING)
                .comment("시스템 자동 생성")
                .build();

        // then
        assertThat(history.getAdminId()).isNull();
        assertThat(history.getComment()).isEqualTo("시스템 자동 생성");
    }

    @Test
    @DisplayName("ReportHistory - 긴 코멘트 저장")
    void reportHistory_LongComment() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .build();

        String longComment = "이 신고는 상세한 검토가 필요합니다. ".repeat(50);

        // when
        ReportHistory history = ReportHistory.builder()
                .historyId("H-001")
                .report(report)
                .adminId("ADMIN-001")
                .actionType(ActionType.REVIEWED)
                .newStatus(ReportStatus.REVIEWING)
                .comment(longComment)
                .build();

        // then
        assertThat(history.getComment()).isEqualTo(longComment);
        assertThat(history.getComment().length()).isGreaterThan(1000);
    }

    @Test
    @DisplayName("setReport - Report 설정")
    void setReport_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .build();

        ReportHistory history = ReportHistory.builder()
                .historyId("H-001")
                .actionType(ActionType.REVIEWED)
                .newStatus(ReportStatus.REVIEWING)
                .build();

        // when
        history.setReport(report);

        // then
        assertThat(history.getReport()).isEqualTo(report);
    }
}
