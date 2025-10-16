package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReportHistory> histories = new ArrayList<>();

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
     * 신고 상태 변경 (히스토리 자동 생성)
     *
     * @param newStatus 새로운 상태
     * @param adminId 처리한 관리자 ID
     * @param comment 변경 사유/코멘트
     * @param historyIdGenerator ID 생성 함수
     */
    public void changeStatus(ReportStatus newStatus, String adminId, String comment,
                           java.util.function.Supplier<String> historyIdGenerator) {
        if (this.status == newStatus) {
            return; // 동일한 상태로 변경 시 무시
        }

        ReportStatus previousStatus = this.status;
        this.status = newStatus;

        // 히스토리 생성 및 연관관계 설정
        ReportHistory history = ReportHistory.createStatusChangeHistory(
                historyIdGenerator.get(),
                this,
                adminId,
                previousStatus,
                newStatus,
                comment
        );

        addHistory(history);
    }

    /**
     * 신고 승인 (제재 적용)
     */
    public void approve(String adminId, String comment, java.util.function.Supplier<String> historyIdGenerator) {
        changeStatus(ReportStatus.APPROVED, adminId, comment, historyIdGenerator);
    }

    /**
     * 신고 거부 (기각)
     */
    public void reject(String adminId, String comment, java.util.function.Supplier<String> historyIdGenerator) {
        changeStatus(ReportStatus.REJECTED, adminId, comment, historyIdGenerator);
    }

    /**
     * 신고 철회 (신고자가 직접 철회)
     */
    public void withdraw(String reporterId, String comment, java.util.function.Supplier<String> historyIdGenerator) {
        if (!this.reporterId.equals(reporterId)) {
            throw new IllegalArgumentException("Only the reporter can withdraw this report");
        }
        if (!isPending()) {
            throw new IllegalStateException("Only pending reports can be withdrawn");
        }
        changeStatus(ReportStatus.WITHDRAWN, null, comment, historyIdGenerator);
    }

    /**
     * 검토 시작
     */
    public void startReview(String adminId, String comment, java.util.function.Supplier<String> historyIdGenerator) {
        changeStatus(ReportStatus.REVIEWING, adminId, comment, historyIdGenerator);
    }

    /**
     * 보류 처리
     */
    public void hold(String adminId, String comment, java.util.function.Supplier<String> historyIdGenerator) {
        changeStatus(ReportStatus.PENDING, adminId, comment, historyIdGenerator);
    }

    /**
     * 히스토리 추가 (연관관계 편의 메서드)
     */
    private void addHistory(ReportHistory history) {
        histories.add(history);
        history.setReport(this);
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
