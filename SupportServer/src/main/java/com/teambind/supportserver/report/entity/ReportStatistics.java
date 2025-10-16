package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 신고 통계 집계 엔티티 (추후 추가 예정)
 * 성능 최적화를 위한 집계 테이블
 */
@Entity
@Table(
        name = "report_statistics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_statistics",
                        columnNames = {"reference_type", "reported_id", "report_category"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportStatistics {

    @Id
    @Column(length = 100)
    @Comment("통계 ID")
    private String statId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "reference_type", referencedColumnName = "reference_type"),
            @JoinColumn(name = "report_category", referencedColumnName = "report_category")
    })
    @Comment("신고 카테고리 참조")
    private ReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 20, insertable = false, updatable = false)
    @Comment("대상 타입")
    private ReferenceType referenceType;

    @Column(nullable = false, length = 100)
    @Comment("신고 대상 ID")
    private String reportedId;

    @Column(name = "report_category", nullable = false, length = 100, insertable = false, updatable = false)
    @Comment("신고 카테고리")
    private String reportCategory;

    @Column(nullable = false)
    @Comment("신고 횟수")
    @Builder.Default
    private Integer reportCount = 0;

    @Column(nullable = false)
    @Comment("최근 신고 일시")
    private LocalDateTime lastReportedAt;

    /**
     * 신고 횟수 증가
     */
    public void incrementReportCount() {
        this.reportCount++;
        this.lastReportedAt = LocalDateTime.now();
    }

    /**
     * 신고 횟수 초기화
     */
    public void resetReportCount() {
        this.reportCount = 0;
    }
}
