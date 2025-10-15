package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ActionType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 신고 처리 이력 엔티티
 */
@Entity
@Table(
        name = "report_history",
        indexes = {
                @Index(name = "idx_report_history_report_id", columnList = "report_id"),
                @Index(name = "idx_report_history_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportHistory {

    @Id
    @Column(length = 100)
    @Comment("이력 ID")
    private String historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, foreignKey = @ForeignKey(name = "fk_report_history_report"))
    @Comment("신고 ID")
    private Report report;

    @Column(length = 100)
    @Comment("처리한 관리자 ID")
    private String adminId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Comment("이전 상태")
    private ReportStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("새 상태")
    private ReportStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Comment("액션 타입")
    private ActionType actionType;

    @Column(columnDefinition = "TEXT")
    @Comment("처리 의견")
    private String comment;

    @Column(nullable = false)
    @Comment("생성 일시")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * 신고 이력 생성 팩토리 메서드
     */
    public static ReportHistory createStatusChangeHistory(
            String historyId,
            Report report,
            String adminId,
            ReportStatus previousStatus,
            ReportStatus newStatus,
            String comment
    ) {
        return ReportHistory.builder()
                .historyId(historyId)
                .report(report)
                .adminId(adminId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .actionType(ActionType.STATUS_CHANGED)
                .comment(comment)
                .build();
    }
}
