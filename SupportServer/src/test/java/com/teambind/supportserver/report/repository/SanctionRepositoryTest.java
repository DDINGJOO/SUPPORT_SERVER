package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.Sanction;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import com.teambind.supportserver.report.entity.enums.SanctionStatus;
import com.teambind.supportserver.report.entity.enums.SanctionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.teambind.supportserver.report.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SanctionRepository 통합 테스트
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("SanctionRepository 통합 테스트")
class SanctionRepositoryTest {

    @Autowired
    private SanctionRepository sanctionRepository;

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
                .status(ReportStatus.APPROVED)
                .build();
        testReport.setCategory(category);
        reportRepository.save(testReport);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("제재 저장 - 정상")
    void saveSanction_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("반복적인 스팸 행위")
                .sanctionedAt(now)
                .expiresAt(now.plusDays(7))
                .build();

        // when
        Sanction savedSanction = sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // then
        Sanction foundSanction = sanctionRepository.findById(savedSanction.getSanctionId()).orElseThrow();
        assertThat(foundSanction.getSanctionId()).isEqualTo("SANCTION-001");
        assertThat(foundSanction.getTargetId()).isEqualTo("USER-002");
        assertThat(foundSanction.getSanctionType()).isEqualTo(SanctionType.SUSPENSION);
        assertThat(foundSanction.getDuration()).isEqualTo(7);
        assertThat(foundSanction.getReason()).isEqualTo("반복적인 스팸 행위");
        assertThat(foundSanction.getStatus()).isEqualTo(SanctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("제재 조회 - ID로 조회")
    void findById_Success() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-002")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("경고")
                .sanctionedAt(LocalDateTime.now())
                .build();
        sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Sanction> foundSanction = sanctionRepository.findById("SANCTION-002");

        // then
        assertThat(foundSanction).isPresent();
        assertThat(foundSanction.get().getSanctionId()).isEqualTo("SANCTION-002");
    }

    @Test
    @DisplayName("제재 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
    void findById_NotFound() {
        // when
        Optional<Sanction> foundSanction = sanctionRepository.findById("NON-EXISTENT-ID");

        // then
        assertThat(foundSanction).isEmpty();
    }

    @Test
    @DisplayName("제재 전체 조회")
    void findAll_Success() {
        // given
        Sanction sanction1 = Sanction.builder()
                .sanctionId("SANCTION-003")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("경고1")
                .sanctionedAt(LocalDateTime.now())
                .build();

        Sanction sanction2 = Sanction.builder()
                .sanctionId("SANCTION-004")
                .report(testReport)
                .targetId("USER-003")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("정지1")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        sanctionRepository.save(sanction1);
        sanctionRepository.save(sanction2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Sanction> sanctions = sanctionRepository.findAll();

        // then
        assertThat(sanctions).hasSize(2);
    }

    @Test
    @DisplayName("제재 삭제 - 정상")
    void deleteSanction_Success() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-005")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("삭제 테스트")
                .sanctionedAt(LocalDateTime.now())
                .build();
        sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // when
        sanctionRepository.deleteById("SANCTION-005");
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Sanction> deletedSanction = sanctionRepository.findById("SANCTION-005");
        assertThat(deletedSanction).isEmpty();
    }

    @Test
    @DisplayName("제재 수정 - 상태 변경")
    void updateSanction_StatusChange() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-006")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("상태 변경 테스트")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(SanctionStatus.ACTIVE)
                .build();
        sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // when
        Sanction foundSanction = sanctionRepository.findById("SANCTION-006").orElseThrow();
        foundSanction.revoke();
        sanctionRepository.save(foundSanction);
        entityManager.flush();
        entityManager.clear();

        // then
        Sanction updatedSanction = sanctionRepository.findById("SANCTION-006").orElseThrow();
        assertThat(updatedSanction.getStatus()).isEqualTo(SanctionStatus.REVOKED);
    }

    @Test
    @DisplayName("Report 연관관계 테스트")
    void reportRelationship_Test() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-007")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("연관관계 테스트")
                .sanctionedAt(LocalDateTime.now())
                .build();
        sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // when
        Sanction foundSanction = sanctionRepository.findById("SANCTION-007").orElseThrow();

        // then
        assertThat(foundSanction.getReport()).isNotNull();
        assertThat(foundSanction.getReport().getReportId()).isEqualTo("REPORT-TEST");
    }

    @Test
    @DisplayName("다양한 제재 타입 저장 및 조회")
    void saveSanctions_WithDifferentTypes() {
        // given
        Sanction warning = createSanction("SANCTION-TYPE-1", SanctionType.WARNING, null);
        Sanction suspension = createSanction("SANCTION-TYPE-2", SanctionType.SUSPENSION, 7);
        Sanction permanentBan = createSanction("SANCTION-TYPE-3", SanctionType.PERMANENT_BAN, null);

        sanctionRepository.saveAll(List.of(warning, suspension, permanentBan));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Sanction> sanctions = sanctionRepository.findAll();

        // then
        assertThat(sanctions).hasSize(3);
        assertThat(sanctions).extracting(Sanction::getSanctionType)
                .containsExactlyInAnyOrder(
                        SanctionType.WARNING,
                        SanctionType.SUSPENSION,
                        SanctionType.PERMANENT_BAN
                );
    }

    @Test
    @DisplayName("다양한 제재 상태 저장 및 조회")
    void saveSanctions_WithDifferentStatuses() {
        // given
        Sanction active = createSanctionWithStatus("SANCTION-STATUS-1", SanctionStatus.ACTIVE);
        Sanction expired = createSanctionWithStatus("SANCTION-STATUS-2", SanctionStatus.EXPIRED);
        Sanction revoked = createSanctionWithStatus("SANCTION-STATUS-3", SanctionStatus.REVOKED);

        sanctionRepository.saveAll(List.of(active, expired, revoked));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Sanction> sanctions = sanctionRepository.findAll();

        // then
        assertThat(sanctions).hasSize(3);
        assertThat(sanctions).extracting(Sanction::getStatus)
                .containsExactlyInAnyOrder(
                        SanctionStatus.ACTIVE,
                        SanctionStatus.EXPIRED,
                        SanctionStatus.REVOKED
                );
    }

    @Test
    @DisplayName("PrePersist 테스트 - sanctionedAt 자동 설정")
    void prePersist_SanctionedAtAutoSet() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-008")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("PrePersist 테스트")
                .build();

        // when
        Sanction savedSanction = sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // then
        Sanction foundSanction = sanctionRepository.findById("SANCTION-008").orElseThrow();
        assertThat(foundSanction.getSanctionedAt()).isNotNull();
        assertThat(foundSanction.getSanctionedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("PrePersist 테스트 - expiresAt 자동 계산")
    void prePersist_ExpiresAtAutoCalculated() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-009")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("만료일 자동 계산 테스트")
                .sanctionedAt(now)
                .build();

        // when
        Sanction savedSanction = sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // then
        Sanction foundSanction = sanctionRepository.findById("SANCTION-009").orElseThrow();
        assertThat(foundSanction.getExpiresAt()).isNotNull();
        assertThat(foundSanction.getExpiresAt()).isAfterOrEqualTo(now.plusDays(7).minusSeconds(1));
    }

    @Test
    @DisplayName("영구 정지 제재 - expiresAt이 null")
    void permanentBan_ExpiresAtIsNull() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-010")
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.PERMANENT_BAN)
                .reason("영구 정지")
                .sanctionedAt(LocalDateTime.now())
                .build();

        // when
        sanctionRepository.save(sanction);
        entityManager.flush();
        entityManager.clear();

        // then
        Sanction foundSanction = sanctionRepository.findById("SANCTION-010").orElseThrow();
        assertThat(foundSanction.getExpiresAt()).isNull();
        assertThat(foundSanction.isPermanent()).isTrue();
    }

    @Test
    @DisplayName("대량 제재 생성 및 조회 테스트")
    void bulkSanctions_CreateAndRetrieve() {
        // given
        for (int i = 1; i <= 100; i++) {
            Sanction sanction = Sanction.builder()
                    .sanctionId("SANCTION-BULK-" + i)
                    .report(testReport)
                    .targetId("USER-" + i)
                    .sanctionType(SanctionType.WARNING)
                    .reason("대량 제재 테스트 " + i)
                    .sanctionedAt(LocalDateTime.now())
                    .build();
            sanctionRepository.save(sanction);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        List<Sanction> sanctions = sanctionRepository.findAll();

        // then
        assertThat(sanctions).hasSize(100);
    }

    @Test
    @DisplayName("제재 기간별 조회 테스트")
    void sanctions_ByDuration() {
        // given
        Sanction shortSanction = createSanction("SANCTION-DUR-1", SanctionType.SUSPENSION, 1);
        Sanction mediumSanction = createSanction("SANCTION-DUR-2", SanctionType.SUSPENSION, 7);
        Sanction longSanction = createSanction("SANCTION-DUR-3", SanctionType.SUSPENSION, 30);

        sanctionRepository.saveAll(List.of(shortSanction, mediumSanction, longSanction));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Sanction> sanctions = sanctionRepository.findAll();

        // then
        assertThat(sanctions).hasSize(3);
        assertThat(sanctions).extracting(Sanction::getDuration)
                .containsExactlyInAnyOrder(1, 7, 30);
    }

    private Sanction createSanction(String sanctionId, SanctionType type, Integer duration) {
        LocalDateTime now = LocalDateTime.now();
        return Sanction.builder()
                .sanctionId(sanctionId)
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(type)
                .duration(duration)
                .reason("테스트 제재")
                .sanctionedAt(now)
                .expiresAt(duration != null ? now.plusDays(duration) : null)
                .build();
    }

    private Sanction createSanctionWithStatus(String sanctionId, SanctionStatus status) {
        return Sanction.builder()
                .sanctionId(sanctionId)
                .report(testReport)
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("테스트 제재")
                .sanctionedAt(LocalDateTime.now())
                .status(status)
                .build();
    }
}
