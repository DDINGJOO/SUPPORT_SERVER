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
	
	/**
	 * -- SETTER --
	 *  신고 설정
	 */
	@Setter
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

    //== 연관관계 편의 메서드 ==//
	
	//== 생성 메서드 (팩토리 메서드) ==//

    /**
     * 상태 변경 이력 생성
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

    /**
     * 검토 완료 이력 생성
     */
    public static ReportHistory createReviewedHistory(
            String historyId,
            Report report,
            String adminId,
            String comment
    ) {
        return ReportHistory.builder()
                .historyId(historyId)
                .report(report)
                .adminId(adminId)
                .actionType(ActionType.REVIEWED)
                .comment(comment)
                .build();
    }

    /**
     * 제재 적용 이력 생성
     */
    public static ReportHistory createSanctionAppliedHistory(
            String historyId,
            Report report,
            String adminId,
            String comment
    ) {
        return ReportHistory.builder()
                .historyId(historyId)
                .report(report)
                .adminId(adminId)
                .actionType(ActionType.SANCTION_APPLIED)
                .comment(comment)
                .build();
    }

    /**
     * 담당자 할당 이력 생성
     */
    public static ReportHistory createAssignedHistory(
            String historyId,
            Report report,
            String adminId,
            String comment
    ) {
        return ReportHistory.builder()
                .historyId(historyId)
                .report(report)
                .adminId(adminId)
                .actionType(ActionType.ASSIGNED)
                .comment(comment)
                .build();
    }

    /**
     * 코멘트 추가 이력 생성
     */
    public static ReportHistory createCommentAddedHistory(
            String historyId,
            Report report,
            String adminId,
            String comment
    ) {
        return ReportHistory.builder()
                .historyId(historyId)
                .report(report)
                .adminId(adminId)
                .actionType(ActionType.COMMENT_ADDED)
                .comment(comment)
                .build();
    }
}
