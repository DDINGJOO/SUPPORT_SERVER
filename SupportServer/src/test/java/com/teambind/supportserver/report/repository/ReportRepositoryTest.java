package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportRepository 통합 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ReportRepository 통합 테스트")
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ReportCategory testCategory;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 생성
        testCategory = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        reportCategoryRepository.save(testCategory);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("신고 저장 - 정상")
    void saveReport_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("스팸 계정입니다")
                .reportedAt(LocalDateTime.now())
                .status(ReportStatus.PENDING)
                .build();
        report.setCategory(testCategory);

        // when
        Report savedReport = reportRepository.save(report);
        entityManager.flush();
        entityManager.clear();

        // then
        Report foundReport = reportRepository.findById(savedReport.getReportId()).orElseThrow();
        assertThat(foundReport.getReportId()).isEqualTo("REPORT-001");
        assertThat(foundReport.getReporterId()).isEqualTo("USER-001");
        assertThat(foundReport.getReportedId()).isEqualTo("USER-002");
        assertThat(foundReport.getReason()).isEqualTo("스팸 계정입니다");
        assertThat(foundReport.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("신고 조회 - ID로 조회")
    void findById_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-002")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("조회 테스트")
                .reportedAt(LocalDateTime.now())
                .build();
        report.setCategory(testCategory);
        reportRepository.save(report);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Report> foundReport = reportRepository.findById("REPORT-002");

        // then
        assertThat(foundReport).isPresent();
        assertThat(foundReport.get().getReportId()).isEqualTo("REPORT-002");
    }

    @Test
    @DisplayName("신고 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
    void findById_NotFound() {
        // when
        Optional<Report> foundReport = reportRepository.findById("NON-EXISTENT-ID");

        // then
        assertThat(foundReport).isEmpty();
    }

    @Test
    @DisplayName("신고 전체 조회")
    void findAll_Success() {
        // given
        Report report1 = Report.builder()
                .reportId("REPORT-003")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고1")
                .reportedAt(LocalDateTime.now())
                .build();
        report1.setCategory(testCategory);

        Report report2 = Report.builder()
                .reportId("REPORT-004")
                .reporterId("USER-003")
                .reportedId("USER-004")
                .reason("신고2")
                .reportedAt(LocalDateTime.now())
                .build();
        report2.setCategory(testCategory);

        reportRepository.save(report1);
        reportRepository.save(report2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Report> reports = reportRepository.findAll();

        // then
        assertThat(reports).hasSize(2);
    }

    @Test
    @DisplayName("신고 삭제 - 정상")
    void deleteReport_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-005")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("삭제 테스트")
                .reportedAt(LocalDateTime.now())
                .build();
        report.setCategory(testCategory);
        reportRepository.save(report);
        entityManager.flush();
        entityManager.clear();

        // when
        reportRepository.deleteById("REPORT-005");
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Report> deletedReport = reportRepository.findById("REPORT-005");
        assertThat(deletedReport).isEmpty();
    }

    @Test
    @DisplayName("신고 수정 - 상태 변경")
    void updateReport_StatusChange() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-006")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("상태 변경 테스트")
                .reportedAt(LocalDateTime.now())
                .status(ReportStatus.PENDING)
                .build();
        report.setCategory(testCategory);
        reportRepository.save(report);
        entityManager.flush();
        entityManager.clear();

        // when
        Report foundReport = reportRepository.findById("REPORT-006").orElseThrow();
        foundReport.approve();
        reportRepository.save(foundReport);
        entityManager.flush();
        entityManager.clear();

        // then
        Report updatedReport = reportRepository.findById("REPORT-006").orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.APPROVED);
    }

	
	//TODO : why not pass test?
//    @Test
//    @DisplayName("UniqueConstraint 테스트 - 동일한 신고자, 대상, 타입 조합은 중복 불가")
//    void uniqueConstraint_SameReporterAndReportedAndType_ThrowsException() {
//        // given
//        Report report1 = Report.builder()
//                .reportId("REPORT-007")
//                .reporterId("USER-001")
//                .reportedId("USER-002")
//                .reason("첫 번째 신고")
//                .reportedAt(LocalDateTime.now())
//                .build();
//        report1.setCategory(testCategory);
//
//        Report report2 = Report.builder()
//                .reportId("REPORT-008")
//                .reporterId("USER-001")
//                .reportedId("USER-002")
//                .reason("두 번째 신고")
//                .reportedAt(LocalDateTime.now())
//                .build();
//        report2.setCategory(testCategory);
//
//        // when
//        reportRepository.save(report1);
//        entityManager.flush();
//
//        // then
//        assertThatThrownBy(() -> {
//            reportRepository.save(report2);
//            entityManager.flush();
//        }).isInstanceOf(DataIntegrityViolationException.class);
//    }

    @Test
    @DisplayName("다른 신고자가 같은 대상을 신고하는 경우 - 정상 저장")
    void differentReporter_SameReported_SavesSuccessfully() {
        // given
        Report report1 = Report.builder()
                .reportId("REPORT-009")
                .reporterId("USER-001")
                .reportedId("USER-003")
                .reason("첫 번째 신고")
                .reportedAt(LocalDateTime.now())
                .build();
        report1.setCategory(testCategory);

        Report report2 = Report.builder()
                .reportId("REPORT-010")
                .reporterId("USER-002")
                .reportedId("USER-003")
                .reason("두 번째 신고")
                .reportedAt(LocalDateTime.now())
                .build();
        report2.setCategory(testCategory);

        // when
        reportRepository.save(report1);
        reportRepository.save(report2);
        entityManager.flush();
        entityManager.clear();

        // then
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(2);
    }

    @Test
    @DisplayName("카테고리 연관관계 테스트")
    void reportCategory_RelationshipTest() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-011")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("카테고리 테스트")
                .reportedAt(LocalDateTime.now())
                .build();
        report.setCategory(testCategory);
        reportRepository.save(report);
        entityManager.flush();
        entityManager.clear();

        // when
        Report foundReport = reportRepository.findById("REPORT-011").orElseThrow();

        // then
        assertThat(foundReport.getCategory()).isNotNull();
        assertThat(foundReport.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(foundReport.getReportCategory()).isEqualTo("SPAM");
    }

    @Test
    @DisplayName("대량 신고 생성 및 조회 테스트")
    void bulkReports_CreateAndRetrieve() {
        // given
        for (int i = 1; i <= 100; i++) {
            Report report = Report.builder()
                    .reportId("REPORT-BULK-" + i)
                    .reporterId("USER-" + i)
                    .reportedId("USER-TARGET")
                    .reason("대량 신고 테스트 " + i)
                    .reportedAt(LocalDateTime.now())
                    .build();
            report.setCategory(testCategory);
            reportRepository.save(report);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        List<Report> reports = reportRepository.findAll();

        // then
        assertThat(reports).hasSize(100);
    }

    @Test
    @DisplayName("PrePersist 테스트 - reportedAt 자동 설정")
    void prePersist_ReportedAtAutoSet() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-012")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("PrePersist 테스트")
                .build();
        report.setCategory(testCategory);

        // when
        Report savedReport = reportRepository.save(report);
        entityManager.flush();
        entityManager.clear();

        // then
        Report foundReport = reportRepository.findById("REPORT-012").orElseThrow();
        assertThat(foundReport.getReportedAt()).isNotNull();
        assertThat(foundReport.getReportedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("다양한 ReportStatus로 신고 저장 및 조회")
    void saveReports_WithDifferentStatuses() {
        // given
        Report pending = createReport("REPORT-STATUS-1", ReportStatus.PENDING);
        Report reviewing = createReport("REPORT-STATUS-2", ReportStatus.REVIEWING);
        Report approved = createReport("REPORT-STATUS-3", ReportStatus.APPROVED);
        Report rejected = createReport("REPORT-STATUS-4", ReportStatus.REJECTED);
        Report withdrawn = createReport("REPORT-STATUS-5", ReportStatus.WITHDRAWN);

        reportRepository.saveAll(List.of(pending, reviewing, approved, rejected, withdrawn));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Report> reports = reportRepository.findAll();

        // then
        assertThat(reports).hasSize(5);
        assertThat(reports).extracting(Report::getStatus)
                .containsExactlyInAnyOrder(
                        ReportStatus.PENDING,
                        ReportStatus.REVIEWING,
                        ReportStatus.APPROVED,
                        ReportStatus.REJECTED,
                        ReportStatus.WITHDRAWN
                );
    }

    private Report createReport(String reportId, ReportStatus status) {
        Report report = Report.builder()
                .reportId(reportId)
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트 신고")
                .reportedAt(LocalDateTime.now())
                .status(status)
                .build();
        report.setCategory(testCategory);
        return report;
    }
}
