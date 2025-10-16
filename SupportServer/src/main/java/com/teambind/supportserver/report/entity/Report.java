package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 신고 엔티티
 */
@Entity
@Table(
        name = "report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_per_user",
                        columnNames = {"reporter_id", "reference_type", "reported_id", "status"}
                )
        },
        indexes = {
                @Index(name = "idx_report_reporter_id", columnList = "reporter_id"),
                @Index(name = "idx_report_reported_id", columnList = "reported_id"),
                @Index(name = "idx_report_reported_at", columnList = "reported_at"),
                @Index(name = "idx_report_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @Column(length = 100)
    @Comment("신고 ID")
    private String reportId;

    @Column(nullable = false, length = 100)
    @Comment("신고자 ID")
    private String reporterId;

    @Column(nullable = false, length = 100)
    @Comment("신고 대상 ID")
    private String reportedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "reference_type", referencedColumnName = "reference_type"),
            @JoinColumn(name = "report_category", referencedColumnName = "report_category")
    })
    @Comment("신고 카테고리 참조")
    private ReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 20, insertable = false, updatable = false)
    @Comment("신고 대상 타입")
    private ReferenceType referenceType;

    @Column(name = "report_category", nullable = false, length = 100, insertable = false, updatable = false)
    @Comment("신고 카테고리")
    private String reportCategory;

    @Column(nullable = false, length = 100)
    @Comment("신고 사유")
    private String reason;

    @Column(nullable = false)
    @Comment("신고 일시")
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("신고 상태")
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now();
        }
    }

    //== 연관관계 편의 메서드 ==//

    /**
     * 카테고리 설정
     */
    public void setCategory(ReportCategory category) {
        this.category = category;
        if (category != null) {
            this.referenceType = category.getId().getReferenceType();
            this.reportCategory = category.getId().getReportCategory();
        }
    }

    //== 비즈니스 로직 ==//

    /**
     * 신고 상태 변경
     */
    public void changeStatus(ReportStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * 신고 승인 (제재 적용)
     */
    public void approve() {
        this.status = ReportStatus.APPROVED;
    }

    /**
     * 신고 거부 (기각)
     */
    public void reject() {
        this.status = ReportStatus.REJECTED;
    }

    /**
     * 신고 철회
     */
    public void withdraw() {
        this.status = ReportStatus.WITHDRAWN;
    }

    /**
     * 검토 시작
     */
    public void startReview() {
        this.status = ReportStatus.REVIEWING;
    }

    /**
     * 신고 상태가 대기 중인지 확인
     */
    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }

    /**
     * 신고 상태가 승인되었는지 확인
     */
    public boolean isApproved() {
        return this.status == ReportStatus.APPROVED;
    }
}
