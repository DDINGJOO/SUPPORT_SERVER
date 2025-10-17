package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.ReportHistory;
import com.teambind.supportserver.report.entity.enums.ActionType;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.teambind.supportserver.common.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportHistoryRepository 통합 테스트
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("ReportHistoryRepository 통합 테스트")
class ReportHistoryRepositoryTest {

    @Autowired
    private ReportHistoryRepository reportHistoryRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Report testReport;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 및 신고 생성
        ReportCategory category = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        reportCategoryRepository.save(category);

        testReport = Report.builder()
                .reportId("REPORT-TEST")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트 신고")
                .reportedAt(LocalDateTime.now())
                .status(ReportStatus.PENDING)
                .build();
        testReport.setCategory(category);
        reportRepository.save(testReport);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("신고 이력 저장 - 정상")
    void saveReportHistory_Success() {
        // given
        ReportHistory history = ReportHistory.builder()
                .historyId("HISTORY-001")
                .report(testReport)
                .adminId("ADMIN-001")
                .previousStatus(ReportStatus.PENDING)
                .newStatus(ReportStatus.REVIEWING)
                .actionType(ActionType.STATUS_CHANGED)
                .comment("검토 시작")
                .build();

        // when
        ReportHistory savedHistory = reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById(savedHistory.getHistoryId()).orElseThrow();
        assertThat(foundHistory.getHistoryId()).isEqualTo("HISTORY-001");
        assertThat(foundHistory.getAdminId()).isEqualTo("ADMIN-001");
        assertThat(foundHistory.getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(foundHistory.getNewStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(foundHistory.getActionType()).isEqualTo(ActionType.STATUS_CHANGED);
        assertThat(foundHistory.getComment()).isEqualTo("검토 시작");
    }

    @Test
    @DisplayName("신고 이력 조회 - ID로 조회")
    void findById_Success() {
        // given
        ReportHistory history = ReportHistory.createStatusChangeHistory(
                "HISTORY-002",
                testReport,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.REVIEWING,
                "조회 테스트"
        );
        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<ReportHistory> foundHistory = reportHistoryRepository.findById("HISTORY-002");

        // then
        assertThat(foundHistory).isPresent();
        assertThat(foundHistory.get().getHistoryId()).isEqualTo("HISTORY-002");
    }

    @Test
    @DisplayName("신고 이력 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
    void findById_NotFound() {
        // when
        Optional<ReportHistory> foundHistory = reportHistoryRepository.findById("NON-EXISTENT-ID");

        // then
        assertThat(foundHistory).isEmpty();
    }

    @Test
    @DisplayName("신고 이력 전체 조회")
    void findAll_Success() {
        // given
        ReportHistory history1 = ReportHistory.createStatusChangeHistory(
                "HISTORY-003",
                testReport,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.REVIEWING,
                "이력1"
        );

        ReportHistory history2 = ReportHistory.createReviewedHistory(
                "HISTORY-004",
                testReport,
                "ADMIN-001",
                "이력2"
        );

        reportHistoryRepository.save(history1);
        reportHistoryRepository.save(history2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<ReportHistory> histories = reportHistoryRepository.findAll();

        // then
        assertThat(histories).hasSize(2);
    }

    @Test
    @DisplayName("신고 이력 삭제 - 정상")
    void deleteReportHistory_Success() {
        // given
        ReportHistory history = ReportHistory.createCommentAddedHistory(
                "HISTORY-005",
                testReport,
                "ADMIN-001",
                "삭제 테스트"
        );
        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // when
        reportHistoryRepository.deleteById("HISTORY-005");
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<ReportHistory> deletedHistory = reportHistoryRepository.findById("HISTORY-005");
        assertThat(deletedHistory).isEmpty();
    }

    @Test
    @DisplayName("Report 연관관계 테스트")
    void reportRelationship_Test() {
        // given
        ReportHistory history = ReportHistory.createStatusChangeHistory(
                "HISTORY-006",
                testReport,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.REVIEWING,
                "연관관계 테스트"
        );
        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // when
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-006").orElseThrow();

        // then
        assertThat(foundHistory.getReport()).isNotNull();
        assertThat(foundHistory.getReport().getReportId()).isEqualTo("REPORT-TEST");
    }

    @Test
    @DisplayName("팩토리 메서드 - createStatusChangeHistory")
    void factoryMethod_CreateStatusChangeHistory() {
        // when
        ReportHistory history = ReportHistory.createStatusChangeHistory(
                "HISTORY-007",
                testReport,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.APPROVED,
                "승인 처리"
        );

        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-007").orElseThrow();
        assertThat(foundHistory.getActionType()).isEqualTo(ActionType.STATUS_CHANGED);
        assertThat(foundHistory.getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(foundHistory.getNewStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("팩토리 메서드 - createReviewedHistory")
    void factoryMethod_CreateReviewedHistory() {
        // when
        ReportHistory history = ReportHistory.createReviewedHistory(
                "HISTORY-008",
                testReport,
                "ADMIN-002",
                "검토 완료"
        );

        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-008").orElseThrow();
        assertThat(foundHistory.getActionType()).isEqualTo(ActionType.REVIEWED);
        assertThat(foundHistory.getAdminId()).isEqualTo("ADMIN-002");
    }

    @Test
    @DisplayName("팩토리 메서드 - createSanctionAppliedHistory")
    void factoryMethod_CreateSanctionAppliedHistory() {
        // when
        ReportHistory history = ReportHistory.createSanctionAppliedHistory(
                "HISTORY-009",
                testReport,
                "ADMIN-003",
                "7일 정지 적용"
        );

        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-009").orElseThrow();
        assertThat(foundHistory.getActionType()).isEqualTo(ActionType.SANCTION_APPLIED);
        assertThat(foundHistory.getComment()).isEqualTo("7일 정지 적용");
    }

    @Test
    @DisplayName("팩토리 메서드 - createAssignedHistory")
    void factoryMethod_CreateAssignedHistory() {
        // when
        ReportHistory history = ReportHistory.createAssignedHistory(
                "HISTORY-010",
                testReport,
                "ADMIN-004",
                "담당자 할당"
        );

        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-010").orElseThrow();
        assertThat(foundHistory.getActionType()).isEqualTo(ActionType.ASSIGNED);
    }

    @Test
    @DisplayName("팩토리 메서드 - createCommentAddedHistory")
    void factoryMethod_CreateCommentAddedHistory() {
        // when
        ReportHistory history = ReportHistory.createCommentAddedHistory(
                "HISTORY-011",
                testReport,
                "ADMIN-005",
                "추가 검토 필요"
        );

        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-011").orElseThrow();
        assertThat(foundHistory.getActionType()).isEqualTo(ActionType.COMMENT_ADDED);
    }

    @Test
    @DisplayName("다양한 ActionType 저장 및 조회")
    void saveHistories_WithDifferentActionTypes() {
        // given
        List<ReportHistory> histories = List.of(
                ReportHistory.createStatusChangeHistory("H-TYPE-1", testReport, "ADMIN-001", ReportStatus.PENDING, ReportStatus.REVIEWING, "상태 변경"),
                ReportHistory.createReviewedHistory("H-TYPE-2", testReport, "ADMIN-001", "검토"),
                ReportHistory.createSanctionAppliedHistory("H-TYPE-3", testReport, "ADMIN-001", "제재"),
                ReportHistory.createAssignedHistory("H-TYPE-4", testReport, "ADMIN-001", "할당"),
                ReportHistory.createCommentAddedHistory("H-TYPE-5", testReport, "ADMIN-001", "코멘트")
        );

        // when
        reportHistoryRepository.saveAll(histories);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportHistory> foundHistories = reportHistoryRepository.findAll();
        assertThat(foundHistories).hasSize(5);
        assertThat(foundHistories).extracting(ReportHistory::getActionType)
                .containsExactlyInAnyOrder(
                        ActionType.STATUS_CHANGED,
                        ActionType.REVIEWED,
                        ActionType.SANCTION_APPLIED,
                        ActionType.ASSIGNED,
                        ActionType.COMMENT_ADDED
                );
    }

    @Test
    @DisplayName("상태 변경 추적 시나리오 - PENDING -> REVIEWING -> APPROVED")
    void statusChangeTracking_Scenario() {
        // given
        ReportHistory history1 = ReportHistory.createStatusChangeHistory(
                "H-TRACK-1",
                testReport,
                "ADMIN-001",
                ReportStatus.PENDING,
                ReportStatus.REVIEWING,
                "검토 시작"
        );

        ReportHistory history2 = ReportHistory.createStatusChangeHistory(
                "H-TRACK-2",
                testReport,
                "ADMIN-001",
                ReportStatus.REVIEWING,
                ReportStatus.APPROVED,
                "승인"
        );

        // when
        reportHistoryRepository.saveAll(List.of(history1, history2));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportHistory> histories = reportHistoryRepository.findAll();
        assertThat(histories).hasSize(2);

        ReportHistory first = reportHistoryRepository.findById("H-TRACK-1").orElseThrow();
        assertThat(first.getPreviousStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(first.getNewStatus()).isEqualTo(ReportStatus.REVIEWING);

        ReportHistory second = reportHistoryRepository.findById("H-TRACK-2").orElseThrow();
        assertThat(second.getPreviousStatus()).isEqualTo(ReportStatus.REVIEWING);
        assertThat(second.getNewStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("adminId 없이 이력 저장 - 시스템 자동 처리")
    void saveHistory_WithoutAdminId() {
        // given
        ReportHistory history = ReportHistory.builder()
                .historyId("HISTORY-012")
                .report(testReport)
                .adminId(null)
                .actionType(ActionType.STATUS_CHANGED)
                .previousStatus(null)
                .newStatus(ReportStatus.PENDING)
                .comment("시스템 자동 생성")
                .build();

        // when
        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-012").orElseThrow();
        assertThat(foundHistory.getAdminId()).isNull();
        assertThat(foundHistory.getComment()).isEqualTo("시스템 자동 생성");
    }

    @Test
    @DisplayName("긴 코멘트 저장 테스트")
    void saveLongComment() {
        // given
        String longComment = "이 신고는 매우 상세한 검토가 필요합니다. ".repeat(100);

        ReportHistory history = ReportHistory.createCommentAddedHistory(
                "HISTORY-013",
                testReport,
                "ADMIN-001",
                longComment
        );

        // when
        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-013").orElseThrow();
        assertThat(foundHistory.getComment()).isEqualTo(longComment);
		assertThat(foundHistory.getComment().length()).isEqualTo(2400);
    }

    @Test
    @DisplayName("PrePersist 테스트 - createdAt 자동 설정")
    void prePersist_CreatedAtAutoSet() {
        // given
        ReportHistory history = ReportHistory.builder()
                .historyId("HISTORY-014")
                .report(testReport)
                .adminId("ADMIN-001")
                .actionType(ActionType.REVIEWED)
                .newStatus(ReportStatus.REVIEWING)
                .comment("PrePersist 테스트")
                .build();

        // when
        reportHistoryRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportHistory foundHistory = reportHistoryRepository.findById("HISTORY-014").orElseThrow();
        assertThat(foundHistory.getCreatedAt()).isNotNull();
        assertThat(foundHistory.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("대량 이력 생성 및 조회 테스트")
    void bulkHistories_CreateAndRetrieve() {
        // given
        for (int i = 1; i <= 100; i++) {
            ReportHistory history = ReportHistory.createCommentAddedHistory(
                    "HISTORY-BULK-" + i,
                    testReport,
                    "ADMIN-001",
                    "대량 이력 " + i
            );
            reportHistoryRepository.save(history);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        List<ReportHistory> histories = reportHistoryRepository.findAll();

        // then
        assertThat(histories).hasSize(100);
    }

    @Test
    @DisplayName("다양한 관리자가 처리한 이력")
    void histories_ByDifferentAdmins() {
        // given
        ReportHistory history1 = ReportHistory.createReviewedHistory("H-ADMIN-1", testReport, "ADMIN-001", "관리자1");
        ReportHistory history2 = ReportHistory.createReviewedHistory("H-ADMIN-2", testReport, "ADMIN-002", "관리자2");
        ReportHistory history3 = ReportHistory.createReviewedHistory("H-ADMIN-3", testReport, "ADMIN-003", "관리자3");

        // when
        reportHistoryRepository.saveAll(List.of(history1, history2, history3));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportHistory> histories = reportHistoryRepository.findAll();
        assertThat(histories).hasSize(3);
        assertThat(histories).extracting(ReportHistory::getAdminId)
                .containsExactlyInAnyOrder("ADMIN-001", "ADMIN-002", "ADMIN-003");
    }

    @Test
    @DisplayName("신고 처리 전체 워크플로우 이력")
    void completeWorkflowHistory() {
        // given - 전체 처리 과정
        ReportHistory created = ReportHistory.createStatusChangeHistory(
                "H-WORKFLOW-1", testReport, null, null, ReportStatus.PENDING, "신고 접수"
        );
        ReportHistory assigned = ReportHistory.createAssignedHistory(
                "H-WORKFLOW-2", testReport, "ADMIN-001", "담당자 배정"
        );
        ReportHistory reviewing = ReportHistory.createStatusChangeHistory(
                "H-WORKFLOW-3", testReport, "ADMIN-001", ReportStatus.PENDING, ReportStatus.REVIEWING, "검토 시작"
        );
        ReportHistory commented = ReportHistory.createCommentAddedHistory(
                "H-WORKFLOW-4", testReport, "ADMIN-001", "추가 확인 필요"
        );
        ReportHistory reviewed = ReportHistory.createReviewedHistory(
                "H-WORKFLOW-5", testReport, "ADMIN-001", "검토 완료"
        );
        ReportHistory approved = ReportHistory.createStatusChangeHistory(
                "H-WORKFLOW-6", testReport, "ADMIN-001", ReportStatus.REVIEWING, ReportStatus.APPROVED, "승인"
        );
        ReportHistory sanctioned = ReportHistory.createSanctionAppliedHistory(
                "H-WORKFLOW-7", testReport, "ADMIN-001", "7일 정지 적용"
        );

        // when
        reportHistoryRepository.saveAll(List.of(
                created, assigned, reviewing, commented, reviewed, approved, sanctioned
        ));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportHistory> histories = reportHistoryRepository.findAll();
        assertThat(histories).hasSize(7);
        assertThat(histories).extracting(ReportHistory::getActionType)
                .containsExactlyInAnyOrder(
                        ActionType.STATUS_CHANGED,
                        ActionType.ASSIGNED,
                        ActionType.STATUS_CHANGED,
                        ActionType.COMMENT_ADDED,
                        ActionType.REVIEWED,
                        ActionType.STATUS_CHANGED,
                        ActionType.SANCTION_APPLIED
                );
    }
}
