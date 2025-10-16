package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportStatistics 엔티티 단위 테스트
 */
@DisplayName("ReportStatistics 엔티티 테스트")
class ReportStatisticsEntityTest {

    @Test
    @DisplayName("ReportStatistics 엔티티 생성 - 정상")
    void createReportStatistics_Success() {
        // given
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.PROFILE, "SPAM"))
                .build();

        LocalDateTime now = LocalDateTime.now();

        // when
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .category(category)
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(now)
                .build();

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getStatId()).isEqualTo("STAT-001");
        assertThat(statistics.getCategory()).isEqualTo(category);
        assertThat(statistics.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(statistics.getReportedId()).isEqualTo("USER-002");
        assertThat(statistics.getReportCategory()).isEqualTo("SPAM");
        assertThat(statistics.getReportCount()).isEqualTo(5);
        assertThat(statistics.getLastReportedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("ReportStatistics 엔티티 생성 - 기본 신고 횟수는 0")
    void createReportStatistics_DefaultReportCount() {
        // when
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.ARTICLE)
                .reportedId("ARTICLE-001")
                .reportCategory("INAPPROPRIATE")
                .lastReportedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(statistics.getReportCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("incrementReportCount - 신고 횟수 증가 및 최근 신고 일시 갱신")
    void incrementReportCount_IncreasesCountAndUpdatesLastReportedAt() throws InterruptedException {
        // given
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(3)
                .lastReportedAt(initialTime)
                .build();

        Thread.sleep(10); // 시간 차이 보장

        // when
        statistics.incrementReportCount();

        // then
        assertThat(statistics.getReportCount()).isEqualTo(4);
        assertThat(statistics.getLastReportedAt()).isAfter(initialTime);
    }

    @Test
    @DisplayName("incrementReportCount - 여러 번 호출 시 정확하게 증가")
    void incrementReportCount_MultipleIncrements() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(0)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when
        statistics.incrementReportCount();
        statistics.incrementReportCount();
        statistics.incrementReportCount();

        // then
        assertThat(statistics.getReportCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("resetReportCount - 신고 횟수 초기화")
    void resetReportCount_ResetsToZero() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-002")
                .reportCategory("SPAM")
                .reportCount(10)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when
        statistics.resetReportCount();

        // then
        assertThat(statistics.getReportCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("ReportStatistics - 다양한 ReferenceType에 대한 통계")
    void createReportStatistics_ForDifferentReferenceTypes() {
        // when
        ReportStatistics profileStats = ReportStatistics.builder()
                .statId("STAT-PROFILE")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-001")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(LocalDateTime.now())
                .build();

        ReportStatistics articleStats = ReportStatistics.builder()
                .statId("STAT-ARTICLE")
                .referenceType(ReferenceType.ARTICLE)
                .reportedId("ARTICLE-001")
                .reportCategory("INAPPROPRIATE")
                .reportCount(3)
                .lastReportedAt(LocalDateTime.now())
                .build();

        ReportStatistics businessStats = ReportStatistics.builder()
                .statId("STAT-BUSINESS")
                .referenceType(ReferenceType.BUSINESS)
                .reportedId("BUSINESS-001")
                .reportCategory("FRAUD")
                .reportCount(7)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(profileStats.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(articleStats.getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(businessStats.getReferenceType()).isEqualTo(ReferenceType.BUSINESS);
    }

    @Test
    @DisplayName("ReportStatistics - 높은 신고 횟수 시나리오")
    void reportStatistics_HighReportCountScenario() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-SPAM")
                .reportCategory("SPAM")
                .reportCount(0)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when - 100번 신고
        for (int i = 0; i < 100; i++) {
            statistics.incrementReportCount();
        }

        // then
        assertThat(statistics.getReportCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("ReportStatistics - 카테고리별 통계 구분")
    void reportStatistics_DifferentCategories() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        ReportStatistics spamStats = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-001")
                .reportCategory("SPAM")
                .reportCount(5)
                .lastReportedAt(now)
                .build();

        ReportStatistics abuseStats = ReportStatistics.builder()
                .statId("STAT-002")
                .referenceType(ReferenceType.PROFILE)
                .reportedId("USER-001")
                .reportCategory("ABUSE")
                .reportCount(3)
                .lastReportedAt(now)
                .build();

        // then - 같은 사용자에 대해 다른 카테고리별 통계 존재
        assertThat(spamStats.getReportedId()).isEqualTo(abuseStats.getReportedId());
        assertThat(spamStats.getReportCategory()).isNotEqualTo(abuseStats.getReportCategory());
        assertThat(spamStats.getReportCount()).isEqualTo(5);
        assertThat(abuseStats.getReportCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("ReportStatistics - 증가 후 리셋 시나리오")
    void reportStatistics_IncrementThenReset() {
        // given
        ReportStatistics statistics = ReportStatistics.builder()
                .statId("STAT-001")
                .referenceType(ReferenceType.ARTICLE)
                .reportedId("ARTICLE-001")
                .reportCategory("INAPPROPRIATE")
                .reportCount(0)
                .lastReportedAt(LocalDateTime.now())
                .build();

        // when
        statistics.incrementReportCount();
        statistics.incrementReportCount();
        statistics.incrementReportCount();
        assertThat(statistics.getReportCount()).isEqualTo(3);

        statistics.resetReportCount();

        // then
        assertThat(statistics.getReportCount()).isEqualTo(0);
    }
}
