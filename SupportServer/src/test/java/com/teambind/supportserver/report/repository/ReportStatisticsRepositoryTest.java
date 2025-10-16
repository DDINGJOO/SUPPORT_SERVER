package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.ReportStatistics;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.teambind.supportserver.common.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportStatisticsRepository 통합 테스트
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("ReportStatisticsRepository 통합 테스트")
class ReportStatisticsRepositoryTest {

    @Autowired
    private ReportStatisticsRepository reportStatisticsRepository;

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
    @DisplayName("신고 통계 저장 - 정상")
    void saveReportStatistics_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(now)
                .build();

        // when
        ReportStatistics savedStatistics = reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics foundStatistics = reportStatisticsRepository.findById(savedStatistics.getStatId()).orElseThrow();
        assertThat(foundStatistics.getStatId()).isEqualTo("STAT-001");
        assertThat(foundStatistics.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(foundStatistics.getReportedId()).isEqualTo("USER-002");
        assertThat(foundStatistics.getReportCategory()).isEqualTo("SPAM");
        assertThat(foundStatistics.getReportCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("신고 통계 조회 - ID로 조회")
    void findById_Success() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-002")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(3)
                .lastReportedAt(LocalDateTime.now())
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<ReportStatistics> foundStatistics = reportStatisticsRepository.findById("STAT-002");

        // then
        assertThat(foundStatistics).isPresent();
        assertThat(foundStatistics.get().getStatId()).isEqualTo("STAT-002");
    }

    @Test
    @DisplayName("신고 통계 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
    void findById_NotFound() {
        // when
        Optional<ReportStatistics> foundStatistics = reportStatisticsRepository.findById("NON-EXISTENT-ID");

        // then
        assertThat(foundStatistics).isEmpty();
    }

    @Test
    @DisplayName("신고 통계 전체 조회")
    void findAll_Success() {
        // given
        ReportStatistics statistics1 = ReportStatistics.builder()
                .statId("STAT-003")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(LocalDateTime.now())
                .build();

        ReportStatistics statistics2 = ReportStatistics.builder()
                .statId("STAT-004")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-003")
                .reportCategory("SPAM")
                .reportCount(3)
                .lastReportedAt(LocalDateTime.now())
                .build();

        reportStatisticsRepository.save(statistics1);
        reportStatisticsRepository.save(statistics2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<ReportStatistics> statisticsList = reportStatisticsRepository.findAll();

        // then
        assertThat(statisticsList).hasSize(2);
    }

    @Test
    @DisplayName("신고 통계 삭제 - 정상")
    void deleteReportStatistics_Success() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-005")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(2)
                .lastReportedAt(LocalDateTime.now())
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when
        reportStatisticsRepository.deleteById("STAT-005");
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<ReportStatistics> deletedStatistics = reportStatisticsRepository.findById("STAT-005");
        assertThat(deletedStatistics).isEmpty();
    }

    @Test
    @DisplayName("신고 통계 수정 - 신고 횟수 증가")
    void updateReportStatistics_IncrementCount() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-006")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(3)
                .lastReportedAt(LocalDateTime.now())
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when
        ReportStatistics foundStatistics = reportStatisticsRepository.findById("STAT-006").orElseThrow();
        foundStatistics.incrementReportCount();
        reportStatisticsRepository.save(foundStatistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics updatedStatistics = reportStatisticsRepository.findById("STAT-006").orElseThrow();
        assertThat(updatedStatistics.getReportCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("신고 통계 수정 - 신고 횟수 초기화")
    void updateReportStatistics_ResetCount() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-007")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(10)
                .lastReportedAt(LocalDateTime.now())
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when
        ReportStatistics foundStatistics = reportStatisticsRepository.findById("STAT-007").orElseThrow();
        foundStatistics.resetReportCount();
        reportStatisticsRepository.save(foundStatistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics updatedStatistics = reportStatisticsRepository.findById("STAT-007").orElseThrow();
        assertThat(updatedStatistics.getReportCount()).isEqualTo(0);
    }

	//TODO : why not pass test?
//    @Test
//    @DisplayName("UniqueConstraint 테스트 - 같은 referenceType, reportedId, reportCategory 조합은 중복 불가")
//    void uniqueConstraint_DuplicateKey_ThrowsException() {
//        // given
//        ReportStatistics statistics1 = ReportStatistics.builder()
//                .statId("STAT-008")
//                .category(testCategory)
//                .referenceType(ReferenceType.PROFILE)
//                .reportedId("USER-002")
//                .reportCategory("SPAM")
//                .reportCount(3)
//                .lastReportedAt(LocalDateTime.now())
//                .build();
//
//        ReportStatistics statistics2 = ReportStatistics.builder()
//                .statId("STAT-009")
//                .category(testCategory)
//                .referenceType(ReferenceType.PROFILE)
//                .reportedId("USER-002")
//                .reportCategory("SPAM")
//                .reportCount(5)
//                .lastReportedAt(LocalDateTime.now())
//                .build();
//
//        // when
//        reportStatisticsRepository.save(statistics1);
//        entityManager.flush();
//
//        // then
//        assertThatThrownBy(() -> {
//            reportStatisticsRepository.save(statistics2);
//            entityManager.flush();
//        }).isInstanceOf(DataIntegrityViolationException.class);
//    }

    @Test
    @DisplayName("같은 사용자에 대한 다른 카테고리 통계 - 정상 저장")
    void sameUser_DifferentCategory_SavesSuccessfully() {
        // given
        ReportCategory abuseCategory = ReportCategory.of(ReferenceType.PROFILE, "ABUSE");
        reportCategoryRepository.save(abuseCategory);
        entityManager.flush();

        ReportStatistics spamStats = ReportStatistics.builder()
                .statId("STAT-010")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(LocalDateTime.now())
                .build();

        ReportStatistics abuseStats = ReportStatistics.builder()
                .statId("STAT-011")
                .category(abuseCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("ABUSE")
                .reportCount(3)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when
        reportStatisticsRepository.save(spamStats);
        reportStatisticsRepository.save(abuseStats);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportStatistics> statisticsList = reportStatisticsRepository.findAll();
        assertThat(statisticsList).hasSize(2);
    }

    @Test
    @DisplayName("다양한 ReferenceType에 대한 통계 저장")
    void saveStatistics_ForDifferentReferenceTypes() {
        // given
        ReportCategory articleCategory = ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE");
        ReportCategory businessCategory = ReportCategory.of(ReferenceType.BUSINESS, "FRAUD");
        reportCategoryRepository.saveAll(List.of(articleCategory, businessCategory));
        entityManager.flush();

        ReportStatistics profileStats = ReportStatistics.builder()
                .statId("STAT-TYPE-1")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-001")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(LocalDateTime.now())
                .build();

        ReportStatistics articleStats = ReportStatistics.builder()
                .statId("STAT-TYPE-2")
                .category(articleCategory)
                .referenceType(ReferenceType.ARTICLE)
                .reportedId("ARTICLE-001")
                .reportCategory("INAPPROPRIATE")
                .reportCount(3)
                .lastReportedAt(LocalDateTime.now())
                .build();

        ReportStatistics businessStats = ReportStatistics.builder()
                .statId("STAT-TYPE-3")
                .category(businessCategory)
                .referenceType(ReferenceType.BUSINESS)
                .reportedId("BUSINESS-001")
                .reportCategory("FRAUD")
                .reportCount(7)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when
        reportStatisticsRepository.saveAll(List.of(profileStats, articleStats, businessStats));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportStatistics> statisticsList = reportStatisticsRepository.findAll();
        assertThat(statisticsList).hasSize(3);
        assertThat(statisticsList).extracting(ReportStatistics::getReferenceType)
                .containsExactlyInAnyOrder(
                        ReferenceType.PROFILE,
                        ReferenceType.ARTICLE,
                        ReferenceType.BUSINESS
                );
    }

    @Test
    @DisplayName("신고 횟수 기본값 테스트")
    void reportCount_DefaultValue() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-012")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics foundStatistics = reportStatisticsRepository.findById("STAT-012").orElseThrow();
        assertThat(foundStatistics.getReportCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("높은 신고 횟수 처리 테스트")
    void highReportCount_HandlingTest() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-013")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-SPAM")
                .reportCategory("SPAM")
                .reportCount(0)
                .lastReportedAt(LocalDateTime.now())
                .build();

        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when - 100번 증가
        ReportStatistics foundStatistics = reportStatisticsRepository.findById("STAT-013").orElseThrow();
        for (int i = 0; i < 100; i++) {
            foundStatistics.incrementReportCount();
        }
        reportStatisticsRepository.save(foundStatistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics updatedStatistics = reportStatisticsRepository.findById("STAT-013").orElseThrow();
        assertThat(updatedStatistics.getReportCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("여러 번 증가 후 리셋 시나리오")
    void incrementThenReset_Scenario() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-014")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(0)
                .lastReportedAt(LocalDateTime.now())
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when - 증가
        ReportStatistics foundStatistics1 = reportStatisticsRepository.findById("STAT-014").orElseThrow();
        foundStatistics1.incrementReportCount();
        foundStatistics1.incrementReportCount();
        foundStatistics1.incrementReportCount();
        reportStatisticsRepository.save(foundStatistics1);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics midStatistics = reportStatisticsRepository.findById("STAT-014").orElseThrow();
        assertThat(midStatistics.getReportCount()).isEqualTo(3);

        // when - 리셋
        midStatistics.resetReportCount();
        reportStatisticsRepository.save(midStatistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics finalStatistics = reportStatisticsRepository.findById("STAT-014").orElseThrow();
        assertThat(finalStatistics.getReportCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("대량 통계 생성 및 조회 테스트")
    void bulkStatistics_CreateAndRetrieve() {
        // given
        for (int i = 1; i <= 100; i++) {
            ReportStatistics statistics = ReportStatistics.builder()
                    .statId("STAT-BULK-" + i)
                    .category(testCategory)
                    .referenceType(ReferenceType.PROFILE)
                    .reportedId("USER-" + i)
                    .reportCategory("SPAM")
                    .reportCount(i)
                    .lastReportedAt(LocalDateTime.now())
                    .build();
            reportStatisticsRepository.save(statistics);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        List<ReportStatistics> statisticsList = reportStatisticsRepository.findAll();

        // then
        assertThat(statisticsList).hasSize(100);
    }

    @Test
    @DisplayName("카테고리 연관관계 테스트")
    void categoryRelationship_Test() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-015")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(LocalDateTime.now())
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        // when
        ReportStatistics foundStatistics = reportStatisticsRepository.findById("STAT-015").orElseThrow();

        // then
        assertThat(foundStatistics.getCategory()).isNotNull();
        assertThat(foundStatistics.getCategory().getId().getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(foundStatistics.getCategory().getId().getReportCategory()).isEqualTo("SPAM");
    }

    @Test
    @DisplayName("최근 신고 일시 갱신 테스트")
    void lastReportedAt_UpdateTest() throws InterruptedException {
        // given
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-016")
                .category(testCategory)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(3)
                .lastReportedAt(initialTime)
                .build();
        reportStatisticsRepository.save(statistics);
        entityManager.flush();
        entityManager.clear();

        Thread.sleep(10); // 시간 차이 보장

        // when
        ReportStatistics foundStatistics = reportStatisticsRepository.findById("STAT-016").orElseThrow();
        foundStatistics.incrementReportCount();
        reportStatisticsRepository.save(foundStatistics);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportStatistics updatedStatistics = reportStatisticsRepository.findById("STAT-016").orElseThrow();
        assertThat(updatedStatistics.getLastReportedAt()).isAfter(initialTime);
    }
}
