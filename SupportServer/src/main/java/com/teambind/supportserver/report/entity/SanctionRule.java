package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.SanctionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 제재 규칙 엔티티
 */
@Entity
@Table(
        name = "sanction_rules",
        indexes = {
                @Index(name = "idx_sanction_rules_reference_type", columnList = "reference_type"),
                @Index(name = "idx_sanction_rules_active", columnList = "is_active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SanctionRule {

    @Id
    @Column(length = 100)
    @Comment("규칙 ID")
    private String ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("대상 타입")
    private ReferenceType referenceType;

    @Column(nullable = false)
    @Comment("신고 임계값 (N회 이상 시)")
    private Integer reportThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("적용할 제재 타입")
    private SanctionType sanctionType;

    @Column
    @Comment("제재 기간 (일 단위, NULL이면 영구)")
    private Integer duration;

    @Column(nullable = false)
    @Comment("규칙 활성화 여부")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 규칙 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 규칙 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}
