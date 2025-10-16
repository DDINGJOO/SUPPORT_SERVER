package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.SanctionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SanctionRule 엔티티 단위 테스트
 */
@DisplayName("SanctionRule 엔티티 테스트")
class SanctionRuleEntityTest {

    @Test
    @DisplayName("SanctionRule 엔티티 생성 - 정상")
    void createSanctionRule_Success() {
        // when
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-001")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .duration(null)
                .build();

        // then
        assertThat(rule).isNotNull();
        assertThat(rule.getRuleId()).isEqualTo("RULE-001");
        assertThat(rule.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(rule.getReportThreshold()).isEqualTo(3);
        assertThat(rule.getSanctionType()).isEqualTo(SanctionType.WARNING);
        assertThat(rule.getDuration()).isNull();
        assertThat(rule.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("SanctionRule 엔티티 생성 - 기본 활성화 상태는 true")
    void createSanctionRule_DefaultActiveStatus() {
        // when
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-001")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        // then
        assertThat(rule.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("SanctionRule 활성화 - isActive가 true로 변경")
    void activateRule_IsActiveChangedToTrue() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-001")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .isActive(false)
                .build();

        // when
        rule.activate();

        // then
        assertThat(rule.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("SanctionRule 비활성화 - isActive가 false로 변경")
    void deactivateRule_IsActiveChangedToFalse() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-001")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .isActive(true)
                .build();

        // when
        rule.deactivate();

        // then
        assertThat(rule.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("SanctionRule 생성 - 경고 규칙 (duration null)")
    void createSanctionRule_WarningWithNullDuration() {
        // when
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-WARNING")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(2)
                .sanctionType(SanctionType.WARNING)
                .duration(null)
                .build();

        // then
        assertThat(rule.getSanctionType()).isEqualTo(SanctionType.WARNING);
        assertThat(rule.getDuration()).isNull();
    }

    @Test
    @DisplayName("SanctionRule 생성 - 정지 규칙 (duration 7일)")
    void createSanctionRule_SuspensionWith7Days() {
        // when
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-SUSPENSION")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        // then
        assertThat(rule.getSanctionType()).isEqualTo(SanctionType.SUSPENSION);
        assertThat(rule.getDuration()).isEqualTo(7);
    }

    @Test
    @DisplayName("SanctionRule 생성 - 영구 정지 규칙 (duration null)")
    void createSanctionRule_PermanentBanWithNullDuration() {
        // when
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-PERMANENT")
                .referenceType(ReferenceType.BUSINESS)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .duration(null)
                .build();

        // then
        assertThat(rule.getSanctionType()).isEqualTo(SanctionType.PERMANENT_BAN);
        assertThat(rule.getDuration()).isNull();
    }

    @Test
    @DisplayName("SanctionRule - 여러 ReferenceType에 대한 규칙 생성")
    void createSanctionRule_ForDifferentReferenceTypes() {
        // when
        SanctionRule profileRule = SanctionRule.builder()
                .ruleId("RULE-PROFILE")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .build();

        SanctionRule articleRule = SanctionRule.builder()
                .ruleId("RULE-ARTICLE")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        SanctionRule businessRule = SanctionRule.builder()
                .ruleId("RULE-BUSINESS")
                .referenceType(ReferenceType.BUSINESS)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .build();

        // then
        assertThat(profileRule.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(articleRule.getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(businessRule.getReferenceType()).isEqualTo(ReferenceType.BUSINESS);
    }

    @Test
    @DisplayName("SanctionRule - 임계값이 다른 여러 규칙 생성")
    void createSanctionRule_WithDifferentThresholds() {
        // when
        SanctionRule lowThreshold = SanctionRule.builder()
                .ruleId("RULE-LOW")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(1)
                .sanctionType(SanctionType.WARNING)
                .build();

        SanctionRule mediumThreshold = SanctionRule.builder()
                .ruleId("RULE-MEDIUM")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        SanctionRule highThreshold = SanctionRule.builder()
                .ruleId("RULE-HIGH")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .build();

        // then
        assertThat(lowThreshold.getReportThreshold()).isEqualTo(1);
        assertThat(mediumThreshold.getReportThreshold()).isEqualTo(5);
        assertThat(highThreshold.getReportThreshold()).isEqualTo(10);
    }
}
