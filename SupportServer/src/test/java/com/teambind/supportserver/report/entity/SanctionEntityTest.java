package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import com.teambind.supportserver.report.entity.enums.SanctionStatus;
import com.teambind.supportserver.report.entity.enums.SanctionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Sanction 엔티티 단위 테스트
 */
@DisplayName("Sanction 엔티티 테스트")
class SanctionEntityTest {

    @Test
    @DisplayName("Sanction 엔티티 생성 - 정상")
    void createSanction_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reason("스팸")
                .status(ReportStatus.APPROVED)
                .build();

        LocalDateTime now = LocalDateTime.now();

        // when
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .report(report)
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("반복적인 스팸 행위")
                .sanctionedAt(now)
                .expiresAt(now.plusDays(7))
                .build();

        // then
        assertThat(sanction).isNotNull();
        assertThat(sanction.getSanctionId()).isEqualTo("SANCTION-001");
        assertThat(sanction.getTargetId()).isEqualTo("USER-002");
        assertThat(sanction.getSanctionType()).isEqualTo(SanctionType.SUSPENSION);
        assertThat(sanction.getDuration()).isEqualTo(7);
        assertThat(sanction.getReason()).isEqualTo("반복적인 스팸 행위");
        assertThat(sanction.getStatus()).isEqualTo(SanctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Sanction 엔티티 생성 - 기본 상태는 ACTIVE")
    void createSanction_DefaultStatus() {
        // when
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.WARNING)
                .reason("경고")
                .sanctionedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(sanction.getStatus()).isEqualTo(SanctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("제재 취소 - 상태가 REVOKED로 변경")
    void revokeSanction_StatusChangedToRevoked() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("제재 취소 테스트")
                .sanctionedAt(LocalDateTime.now())
                .status(SanctionStatus.ACTIVE)
                .build();

        // when
        sanction.revoke();

        // then
        assertThat(sanction.getStatus()).isEqualTo(SanctionStatus.REVOKED);
    }

    @Test
    @DisplayName("제재 만료 - 상태가 EXPIRED로 변경")
    void expireSanction_StatusChangedToExpired() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("제재 만료 테스트")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(SanctionStatus.ACTIVE)
                .build();

        // when
        sanction.expire();

        // then
        assertThat(sanction.getStatus()).isEqualTo(SanctionStatus.EXPIRED);
    }

    @Test
    @DisplayName("isActive - 제재가 활성 상태이고 만료되지 않았을 때 true 반환")
    void isActive_WhenActiveAndNotExpired_ReturnsTrue() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("활성 제재")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(SanctionStatus.ACTIVE)
                .build();

        // when & then
        assertThat(sanction.isActive()).isTrue();
    }

    @Test
    @DisplayName("isActive - 제재가 활성 상태지만 만료 시간이 지났을 때 false 반환")
    void isActive_WhenActiveButExpired_ReturnsFalse() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("만료된 제재")
                .sanctionedAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(3))
                .status(SanctionStatus.ACTIVE)
                .build();

        // when & then
        assertThat(sanction.isActive()).isFalse();
    }

    @Test
    @DisplayName("isActive - 제재가 REVOKED 상태일 때 false 반환")
    void isActive_WhenRevoked_ReturnsFalse() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("취소된 제재")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(SanctionStatus.REVOKED)
                .build();

        // when & then
        assertThat(sanction.isActive()).isFalse();
    }

    @Test
    @DisplayName("isExpired - 만료 시간이 지났을 때 true 반환")
    void isExpired_WhenExpired_ReturnsTrue() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("만료된 제재")
                .sanctionedAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(3))
                .status(SanctionStatus.ACTIVE)
                .build();

        // when & then
        assertThat(sanction.isExpired()).isTrue();
    }

    @Test
    @DisplayName("isExpired - 상태가 EXPIRED일 때 true 반환")
    void isExpired_WhenStatusExpired_ReturnsTrue() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("만료 처리된 제재")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(SanctionStatus.EXPIRED)
                .build();

        // when & then
        assertThat(sanction.isExpired()).isTrue();
    }

    @Test
    @DisplayName("isPermanent - 제재 타입이 PERMANENT_BAN일 때 true 반환")
    void isPermanent_WhenTypeIsPermanentBan_ReturnsTrue() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.PERMANENT_BAN)
                .reason("영구 정지")
                .sanctionedAt(LocalDateTime.now())
                .build();

        // when & then
        assertThat(sanction.isPermanent()).isTrue();
    }

    @Test
    @DisplayName("isPermanent - expiresAt이 null일 때 true 반환")
    void isPermanent_WhenExpiresAtIsNull_ReturnsTrue() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .reason("만료일 없는 제재")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(null)
                .build();

        // when & then
        assertThat(sanction.isPermanent()).isTrue();
    }

    @Test
    @DisplayName("isPermanent - 임시 제재일 때 false 반환")
    void isPermanent_WhenTemporary_ReturnsFalse() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("임시 제재")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // when & then
        assertThat(sanction.isPermanent()).isFalse();
    }

    @Test
    @DisplayName("getRemainingDays - 남은 일수를 정확하게 계산")
    void getRemainingDays_CalculatesCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("7일 제재")
                .sanctionedAt(now)
                .expiresAt(now.plusDays(7))
                .build();

        // when
        long remainingDays = sanction.getRemainingDays();

        // then
        assertThat(remainingDays).isBetween(6L, 7L);
    }

    @Test
    @DisplayName("getRemainingDays - 만료된 제재는 0 반환")
    void getRemainingDays_WhenExpired_ReturnsZero() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .reason("만료된 제재")
                .sanctionedAt(now.minusDays(10))
                .expiresAt(now.minusDays(3))
                .build();

        // when
        long remainingDays = sanction.getRemainingDays();

        // then
        assertThat(remainingDays).isEqualTo(0L);
    }

    @Test
    @DisplayName("getRemainingDays - 영구 제재는 -1 반환")
    void getRemainingDays_WhenPermanent_ReturnsMinusOne() {
        // given
        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-002")
                .sanctionType(SanctionType.PERMANENT_BAN)
                .reason("영구 정지")
                .sanctionedAt(LocalDateTime.now())
                .expiresAt(null)
                .build();

        // when
        long remainingDays = sanction.getRemainingDays();

        // then
        assertThat(remainingDays).isEqualTo(-1L);
    }

    @Test
    @DisplayName("setReport - Report 설정 시 targetId 자동 설정")
    void setReport_AutoSetTargetId() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .build();

        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .sanctionType(SanctionType.WARNING)
                .reason("경고")
                .sanctionedAt(LocalDateTime.now())
                .build();

        // when
        sanction.setReport(report);

        // then
        assertThat(sanction.getReport()).isEqualTo(report);
        assertThat(sanction.getTargetId()).isEqualTo("USER-002");
    }

    @Test
    @DisplayName("setReport - targetId가 이미 설정된 경우 변경하지 않음")
    void setReport_DoesNotOverrideExistingTargetId() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("신고")
                .build();

        Sanction sanction = Sanction.builder()
                .sanctionId("SANCTION-001")
                .targetId("USER-003")
                .sanctionType(SanctionType.WARNING)
                .reason("경고")
                .sanctionedAt(LocalDateTime.now())
                .build();

        // when
        sanction.setReport(report);

        // then
        assertThat(sanction.getReport()).isEqualTo(report);
        assertThat(sanction.getTargetId()).isEqualTo("USER-003");
    }
}
