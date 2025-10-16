package com.teambind.supportserver.report.dto.response;

import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 신고 응답 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private String reportId;
    private String reporterId;
    private String reportedId;
    private ReferenceType referenceType;
    private String reportCategory;
    private String reason;
    private LocalDateTime reportedAt;
    private ReportStatus status;

    /**
     * Entity -> DTO 변환
     */
    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporterId())
                .reportedId(report.getReportedId())
                .referenceType(report.getReferenceType())
                .reportCategory(report.getReportCategory())
                .reason(report.getReason())
                .reportedAt(report.getReportedAt())
                .status(report.getStatus())
                .build();
    }
}
