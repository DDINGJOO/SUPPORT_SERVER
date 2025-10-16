package com.teambind.supportserver.report.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teambind.supportserver.report.config.QueryDslConfig;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportRepositoryCustom 통합 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({ReportRepositoryImpl.class, QueryDslConfig.class})
@DisplayName("ReportRepositoryCustom 통합 테스트")
class ReportRepositoryCustomTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ReportCategory profileCategory;
    private ReportCategory articleCategory;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 생성
        profileCategory = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        articleCategory = ReportCategory.of(ReferenceType.ARTICLE, "ABUSE");
        reportCategoryRepository.save(profileCategory);
        reportCategoryRepository.save(articleCategory);

        // 테스트 데이터 생성
        createTestReports();

        entityManager.flush();
        entityManager.clear();
    }

    private void createTestReports() {
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);

        // PENDING 상태 신고 (PROFILE)
        for (int i = 1; i <= 5; i++) {
            Report report = Report.builder()
                    .reportId("PENDING-" + i)
                    .reporterId("USER-" + i)
                    .reportedId("TARGET-1")
                    .reason("PENDING 테스트 " + i)
                    .reportedAt(baseTime.plusDays(i))
                    .status(ReportStatus.PENDING)
                    .build();
            report.setCategory(profileCategory);
            reportRepository.save(report);
        }

        // REVIEWING 상태 신고 (PROFILE)
        for (int i = 1; i <= 3; i++) {
            Report report = Report.builder()
                    .reportId("REVIEWING-" + i)
                    .reporterId("USER-" + (i + 10))
                    .reportedId("TARGET-2")
                    .reason("REVIEWING 테스트 " + i)
                    .reportedAt(baseTime.plusDays(i + 10))
                    .status(ReportStatus.REVIEWING)
                    .build();
            report.setCategory(profileCategory);
            reportRepository.save(report);
        }

        // APPROVED 상태 신고 (ARTICLE)
        for (int i = 1; i <= 4; i++) {
            Report report = Report.builder()
                    .reportId("APPROVED-" + i)
                    .reporterId("USER-" + (i + 20))
                    .reportedId("TARGET-3")
                    .reason("APPROVED 테스트 " + i)
                    .reportedAt(baseTime.plusDays(i + 20))
                    .status(ReportStatus.APPROVED)
                    .build();
            report.setCategory(articleCategory);
            reportRepository.save(report);
        }

        // REJECTED 상태 신고 (ARTICLE)
        for (int i = 1; i <= 2; i++) {
            Report report = Report.builder()
                    .reportId("REJECTED-" + i)
                    .reporterId("USER-" + (i + 30))
                    .reportedId("TARGET-4")
                    .reason("REJECTED 테스트 " + i)
                    .reportedAt(baseTime.plusDays(i + 30))
                    .status(ReportStatus.REJECTED)
                    .build();
            report.setCategory(articleCategory);
            reportRepository.save(report);
        }
    }

    @Test
    @DisplayName("커서 페이징 - 필터 없이 전체 조회 (신고일 내림차순)")
    void findReportsWithCursor_NoFilter_ReportedAtDesc() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(5)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(6); // size + 1
        assertThat(reports.get(0).getReportedAt()).isAfter(reports.get(1).getReportedAt());
    }

    @Test
    @DisplayName("커서 페이징 - 상태 필터링 (PENDING)")
    void findReportsWithCursor_FilterByStatus() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .status(ReportStatus.PENDING)
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(10)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(5);
        assertThat(reports).allMatch(r -> r.getStatus() == ReportStatus.PENDING);
    }

    @Test
    @DisplayName("커서 페이징 - ReferenceType 필터링 (PROFILE)")
    void findReportsWithCursor_FilterByReferenceType() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .referenceType(ReferenceType.PROFILE)
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(20)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(8); // 5 PENDING + 3 REVIEWING
        assertThat(reports).allMatch(r -> r.getReferenceType() == ReferenceType.PROFILE);
    }

    @Test
    @DisplayName("커서 페이징 - ReportCategory 필터링")
    void findReportsWithCursor_FilterByReportCategory() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .reportCategory("SPAM")
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(20)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(8);
        assertThat(reports).allMatch(r -> r.getReportCategory().equals("SPAM"));
    }

    @Test
    @DisplayName("커서 페이징 - 복합 필터링 (상태 + ReferenceType)")
    void findReportsWithCursor_MultipleFilters() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .status(ReportStatus.APPROVED)
                .referenceType(ReferenceType.ARTICLE)
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(10)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(4);
        assertThat(reports).allMatch(r -> r.getStatus() == ReportStatus.APPROVED);
        assertThat(reports).allMatch(r -> r.getReferenceType() == ReferenceType.ARTICLE);
    }

    @Test
    @DisplayName("커서 페이징 - 신고일 오름차순 정렬")
    void findReportsWithCursor_SortByReportedAtAsc() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .status(ReportStatus.PENDING)
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.ASC)
                .size(10)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(5);
        for (int i = 0; i < reports.size() - 1; i++) {
            assertThat(reports.get(i).getReportedAt()).isBefore(reports.get(i + 1).getReportedAt());
        }
    }

    @Test
    @DisplayName("커서 페이징 - 상태 기준 정렬 (내림차순)")
    void findReportsWithCursor_SortByStatusDesc() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .sortType(ReportSearchRequest.SortType.STATUS)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(5)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(6); // size + 1
    }

    @Test
    @DisplayName("커서 페이징 - 커서 사용 (신고일 기준)")
    void findReportsWithCursor_WithCursor_ReportedAt() {
        // given - 첫 번째 페이지 조회
        ReportSearchRequest firstRequest = ReportSearchRequest.builder()
                .status(ReportStatus.PENDING)
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(2)
                .build();

        List<Report> firstPage = reportRepository.findReportsWithCursor(firstRequest);
        String cursor = firstPage.get(1).getReportedAt().toString(); // 두 번째 항목의 시간

        // when - 두 번째 페이지 조회
        ReportSearchRequest secondRequest = ReportSearchRequest.builder()
                .status(ReportStatus.PENDING)
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .cursor(cursor)
                .size(2)
                .build();

        List<Report> secondPage = reportRepository.findReportsWithCursor(secondRequest);

        // then
        assertThat(secondPage).isNotEmpty();
        assertThat(secondPage.get(0).getReportedAt()).isBefore(LocalDateTime.parse(cursor));
    }

    @Test
    @DisplayName("신고자 ID로 조회")
    void findByReporterId() {
        // when
        List<Report> reports = reportRepository.findByReporterId("USER-1");

        // then
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getReporterId()).isEqualTo("USER-1");
    }

    @Test
    @DisplayName("신고 대상 ID로 조회")
    void findByReportedId() {
        // when
        List<Report> reports = reportRepository.findByReportedId("TARGET-1");

        // then
        assertThat(reports).hasSize(5);
        assertThat(reports).allMatch(r -> r.getReportedId().equals("TARGET-1"));
    }

    @Test
    @DisplayName("존재하지 않는 신고자로 조회 - 빈 리스트 반환")
    void findByReporterId_NotFound() {
        // when
        List<Report> reports = reportRepository.findByReporterId("NON-EXISTENT-USER");

        // then
        assertThat(reports).isEmpty();
    }

    @Test
    @DisplayName("커서 페이징 - 페이지 크기 제한 확인")
    void findReportsWithCursor_PageSizeLimit() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .sortType(ReportSearchRequest.SortType.REPORTED_AT)
                .sortDirection(ReportSearchRequest.SortDirection.DESC)
                .size(3)
                .build();

        // when
        List<Report> reports = reportRepository.findReportsWithCursor(request);

        // then
        assertThat(reports).hasSize(4); // size + 1 for hasNext check
    }
}
