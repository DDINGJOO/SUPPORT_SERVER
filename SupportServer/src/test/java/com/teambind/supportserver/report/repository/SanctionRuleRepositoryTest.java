package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.SanctionRule;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.SanctionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.teambind.supportserver.common.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SanctionRuleRepository 통합 테스트
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("SanctionRuleRepository 통합 테스트")
class SanctionRuleRepositoryTest {

    @Autowired
    private SanctionRuleRepository sanctionRuleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("제재 규칙 저장 - 정상")
    void saveSanctionRule_Success() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-001")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .duration(null)
                .build();

        // when
        SanctionRule savedRule = sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule foundRule = sanctionRuleRepository.findById(savedRule.getRuleId()).orElseThrow();
        assertThat(foundRule.getRuleId()).isEqualTo("RULE-001");
        assertThat(foundRule.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(foundRule.getReportThreshold()).isEqualTo(3);
        assertThat(foundRule.getSanctionType()).isEqualTo(SanctionType.WARNING);
        assertThat(foundRule.getDuration()).isNull();
        assertThat(foundRule.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("제재 규칙 조회 - ID로 조회")
    void findById_Success() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-002")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<SanctionRule> foundRule = sanctionRuleRepository.findById("RULE-002");

        // then
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getRuleId()).isEqualTo("RULE-002");
    }

    @Test
    @DisplayName("제재 규칙 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
    void findById_NotFound() {
        // when
        Optional<SanctionRule> foundRule = sanctionRuleRepository.findById("NON-EXISTENT-ID");

        // then
        assertThat(foundRule).isEmpty();
    }

    @Test
    @DisplayName("제재 규칙 전체 조회")
    void findAll_Success() {
        // given
        SanctionRule rule1 = SanctionRule.builder()
                .ruleId("RULE-003")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .build();

        SanctionRule rule2 = SanctionRule.builder()
                .ruleId("RULE-004")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        sanctionRuleRepository.save(rule1);
        sanctionRuleRepository.save(rule2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<SanctionRule> rules = sanctionRuleRepository.findAll();

        // then
        assertThat(rules).hasSize(2);
    }

    @Test
    @DisplayName("제재 규칙 삭제 - 정상")
    void deleteSanctionRule_Success() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-005")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .build();
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // when
        sanctionRuleRepository.deleteById("RULE-005");
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<SanctionRule> deletedRule = sanctionRuleRepository.findById("RULE-005");
        assertThat(deletedRule).isEmpty();
    }

    @Test
    @DisplayName("제재 규칙 수정 - 활성화 상태 변경")
    void updateSanctionRule_ActivationChange() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-006")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .isActive(true)
                .build();
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // when
        SanctionRule foundRule = sanctionRuleRepository.findById("RULE-006").orElseThrow();
        foundRule.deactivate();
        sanctionRuleRepository.save(foundRule);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule updatedRule = sanctionRuleRepository.findById("RULE-006").orElseThrow();
        assertThat(updatedRule.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("경고 규칙 저장 - duration null")
    void saveWarningRule_DurationNull() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-WARNING-001")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(2)
                .sanctionType(SanctionType.WARNING)
                .duration(null)
                .build();

        // when
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule foundRule = sanctionRuleRepository.findById("RULE-WARNING-001").orElseThrow();
        assertThat(foundRule.getSanctionType()).isEqualTo(SanctionType.WARNING);
        assertThat(foundRule.getDuration()).isNull();
    }

    @Test
    @DisplayName("정지 규칙 저장 - duration 있음")
    void saveSuspensionRule_WithDuration() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-SUSPENSION-001")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        // when
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule foundRule = sanctionRuleRepository.findById("RULE-SUSPENSION-001").orElseThrow();
        assertThat(foundRule.getSanctionType()).isEqualTo(SanctionType.SUSPENSION);
        assertThat(foundRule.getDuration()).isEqualTo(7);
    }

    @Test
    @DisplayName("영구 정지 규칙 저장 - duration null")
    void savePermanentBanRule_DurationNull() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-PERMANENT-001")
                .referenceType(ReferenceType.BUSINESS)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .duration(null)
                .build();

        // when
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule foundRule = sanctionRuleRepository.findById("RULE-PERMANENT-001").orElseThrow();
        assertThat(foundRule.getSanctionType()).isEqualTo(SanctionType.PERMANENT_BAN);
        assertThat(foundRule.getDuration()).isNull();
    }

    @Test
    @DisplayName("다양한 ReferenceType에 대한 규칙 저장")
    void saveRules_ForDifferentReferenceTypes() {
        // given
        SanctionRule profileRule = SanctionRule.builder()
                .ruleId("RULE-PROFILE-001")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .build();

        SanctionRule articleRule = SanctionRule.builder()
                .ruleId("RULE-ARTICLE-001")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        SanctionRule businessRule = SanctionRule.builder()
                .ruleId("RULE-BUSINESS-001")
                .referenceType(ReferenceType.BUSINESS)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .build();

        // when
        sanctionRuleRepository.saveAll(List.of(profileRule, articleRule, businessRule));
        entityManager.flush();
        entityManager.clear();

        // then
        List<SanctionRule> rules = sanctionRuleRepository.findAll();
        assertThat(rules).hasSize(3);
        assertThat(rules).extracting(SanctionRule::getReferenceType)
                .containsExactlyInAnyOrder(
                        ReferenceType.PROFILE,
                        ReferenceType.ARTICLE,
                        ReferenceType.BUSINESS
                );
    }

    @Test
    @DisplayName("다양한 임계값에 대한 규칙 저장")
    void saveRules_WithDifferentThresholds() {
        // given
        SanctionRule lowThreshold = SanctionRule.builder()
                .ruleId("RULE-THRESHOLD-LOW")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(1)
                .sanctionType(SanctionType.WARNING)
                .build();

        SanctionRule mediumThreshold = SanctionRule.builder()
                .ruleId("RULE-THRESHOLD-MEDIUM")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        SanctionRule highThreshold = SanctionRule.builder()
                .ruleId("RULE-THRESHOLD-HIGH")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .build();

        // when
        sanctionRuleRepository.saveAll(List.of(lowThreshold, mediumThreshold, highThreshold));
        entityManager.flush();
        entityManager.clear();

        // then
        List<SanctionRule> rules = sanctionRuleRepository.findAll();
        assertThat(rules).hasSize(3);
        assertThat(rules).extracting(SanctionRule::getReportThreshold)
                .containsExactlyInAnyOrder(1, 5, 10);
    }

    @Test
    @DisplayName("활성화/비활성화 규칙 혼합 저장")
    void saveRules_ActiveAndInactive() {
        // given
        SanctionRule activeRule = SanctionRule.builder()
                .ruleId("RULE-ACTIVE")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .isActive(true)
                .build();

        SanctionRule inactiveRule = SanctionRule.builder()
                .ruleId("RULE-INACTIVE")
                .referenceType(ReferenceType.ARTICLE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .isActive(false)
                .build();

        // when
        sanctionRuleRepository.saveAll(List.of(activeRule, inactiveRule));
        entityManager.flush();
        entityManager.clear();

        // then
        List<SanctionRule> rules = sanctionRuleRepository.findAll();
        assertThat(rules).hasSize(2);

        long activeCount = rules.stream().filter(SanctionRule::getIsActive).count();
        long inactiveCount = rules.stream().filter(r -> !r.getIsActive()).count();

        assertThat(activeCount).isEqualTo(1);
        assertThat(inactiveCount).isEqualTo(1);
    }

    @Test
    @DisplayName("단계별 제재 규칙 시나리오 - 경고 -> 정지 -> 영구정지")
    void saveRules_EscalationScenario() {
        // given - 같은 ReferenceType에 대한 단계별 규칙
        SanctionRule warningRule = SanctionRule.builder()
                .ruleId("RULE-ESCALATION-1")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .build();

        SanctionRule suspensionRule = SanctionRule.builder()
                .ruleId("RULE-ESCALATION-2")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(7)
                .build();

        SanctionRule permanentBanRule = SanctionRule.builder()
                .ruleId("RULE-ESCALATION-3")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(10)
                .sanctionType(SanctionType.PERMANENT_BAN)
                .build();

        // when
        sanctionRuleRepository.saveAll(List.of(warningRule, suspensionRule, permanentBanRule));
        entityManager.flush();
        entityManager.clear();

        // then
        List<SanctionRule> rules = sanctionRuleRepository.findAll();
        assertThat(rules).hasSize(3);
        assertThat(rules).allMatch(r -> r.getReferenceType() == ReferenceType.PROFILE);
        assertThat(rules).extracting(SanctionRule::getSanctionType)
                .containsExactlyInAnyOrder(
                        SanctionType.WARNING,
                        SanctionType.SUSPENSION,
                        SanctionType.PERMANENT_BAN
                );
    }

    @Test
    @DisplayName("다양한 제재 기간 설정 테스트")
    void saveRules_WithDifferentDurations() {
        // given
        SanctionRule short1Day = createSuspensionRule("RULE-DUR-1", 1);
        SanctionRule short3Days = createSuspensionRule("RULE-DUR-3", 3);
        SanctionRule medium7Days = createSuspensionRule("RULE-DUR-7", 7);
        SanctionRule medium14Days = createSuspensionRule("RULE-DUR-14", 14);
        SanctionRule long30Days = createSuspensionRule("RULE-DUR-30", 30);

        // when
        sanctionRuleRepository.saveAll(List.of(short1Day, short3Days, medium7Days, medium14Days, long30Days));
        entityManager.flush();
        entityManager.clear();

        // then
        List<SanctionRule> rules = sanctionRuleRepository.findAll();
        assertThat(rules).hasSize(5);
        assertThat(rules).extracting(SanctionRule::getDuration)
                .containsExactlyInAnyOrder(1, 3, 7, 14, 30);
    }

    @Test
    @DisplayName("규칙 활성화/비활성화 토글 테스트")
    void toggleRuleActivation() {
        // given
        SanctionRule rule = SanctionRule.builder()
                .ruleId("RULE-TOGGLE")
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(3)
                .sanctionType(SanctionType.WARNING)
                .isActive(true)
                .build();
        sanctionRuleRepository.save(rule);
        entityManager.flush();
        entityManager.clear();

        // when - 비활성화
        SanctionRule foundRule1 = sanctionRuleRepository.findById("RULE-TOGGLE").orElseThrow();
        foundRule1.deactivate();
        sanctionRuleRepository.save(foundRule1);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule deactivatedRule = sanctionRuleRepository.findById("RULE-TOGGLE").orElseThrow();
        assertThat(deactivatedRule.getIsActive()).isFalse();

        // when - 다시 활성화
        deactivatedRule.activate();
        sanctionRuleRepository.save(deactivatedRule);
        entityManager.flush();
        entityManager.clear();

        // then
        SanctionRule reactivatedRule = sanctionRuleRepository.findById("RULE-TOGGLE").orElseThrow();
        assertThat(reactivatedRule.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("대량 규칙 생성 및 조회 테스트")
    void bulkRules_CreateAndRetrieve() {
        // given
        for (int i = 1; i <= 100; i++) {
            SanctionRule rule = SanctionRule.builder()
                    .ruleId("RULE-BULK-" + i)
                    .referenceType(ReferenceType.PROFILE)
                    .reportThreshold(i)
                    .sanctionType(SanctionType.WARNING)
                    .build();
            sanctionRuleRepository.save(rule);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        List<SanctionRule> rules = sanctionRuleRepository.findAll();

        // then
        assertThat(rules).hasSize(100);
    }

    private SanctionRule createSuspensionRule(String ruleId, Integer duration) {
        return SanctionRule.builder()
                .ruleId(ruleId)
                .referenceType(ReferenceType.PROFILE)
                .reportThreshold(5)
                .sanctionType(SanctionType.SUSPENSION)
                .duration(duration)
                .build();
    }
}
