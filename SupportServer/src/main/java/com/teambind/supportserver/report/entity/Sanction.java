package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.SanctionStatus;
import com.teambind.supportserver.report.entity.enums.SanctionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 제재 엔티티
 */
@Entity
@Table(
        name = "sanctions",
        indexes = {
                @Index(name = "idx_sanctions_target_id", columnList = "target_id"),
                @Index(name = "idx_sanctions_status", columnList = "status"),
                @Index(name = "idx_sanctions_expires_at", columnList = "expires_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Sanction {

    @Id
    @Column(length = 100)
    @Comment("제재 ID")
    private String sanctionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sanctions_report"))
    @Comment("연관 신고 ID")
    private Report report;

    @Column(nullable = false, length = 100)
    @Comment("제재 대상 ID")
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("제재 타입")
    private SanctionType sanctionType;

    @Column
    @Comment("제재 기간 (일 단위)")
    private Integer duration;

    @Column(nullable = false, length = 500)
    @Comment("제재 사유")
    private String reason;

    @Column(nullable = false)
    @Comment("제재 시작 일시")
    private LocalDateTime sanctionedAt;

    @Column
    @Comment("제재 만료 일시")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("제재 상태")
    @Builder.Default
    private SanctionStatus status = SanctionStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        if (sanctionedAt == null) {
            sanctionedAt = LocalDateTime.now();
        }
        if (duration != null && expiresAt == null) {
            expiresAt = sanctionedAt.plusDays(duration);
        }
    }

    //== 연관관계 편의 메서드 ==//

    /**
     * 신고 설정
     */
    public void setReport(Report report) {
        this.report = report;
        if (report != null && this.targetId == null) {
            this.targetId = report.getReportedId();
        }
    }

    //== 비즈니스 로직 ==//

    /**
     * 제재 취소
     */
    public void revoke() {
        this.status = SanctionStatus.REVOKED;
    }

    /**
     * 제재 만료 처리
     */
    public void expire() {
        this.status = SanctionStatus.EXPIRED;
    }

    /**
     * 제재가 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == SanctionStatus.ACTIVE &&
                (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    /**
     * 제재가 만료되었는지 확인
     */
    public boolean isExpired() {
        return status == SanctionStatus.EXPIRED ||
                (expiresAt != null && expiresAt.isBefore(LocalDateTime.now()));
    }

    /**
     * 제재가 영구 정지인지 확인
     */
    public boolean isPermanent() {
        return sanctionType == SanctionType.PERMANENT_BAN || expiresAt == null;
    }

    /**
     * 남은 제재 일수 계산
     */
    public long getRemainingDays() {
        if (expiresAt == null) {
            return -1; // 영구
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            return 0; // 만료됨
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
    }
}
