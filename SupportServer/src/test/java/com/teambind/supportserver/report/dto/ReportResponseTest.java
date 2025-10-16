package com.teambind.supportserver.report.dto;

import com.teambind.supportserver.report.dto.response.ReportResponse;
import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportResponse DTO 테스트
 */
@DisplayName("ReportResponse DTO 테스트")
class ReportResponseTest {

    @Test
    @DisplayName("Entity -> DTO 변환 테스트")
    void from_EntityToDto() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.PROFILE, "SPAM"))
                .build();

        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("테스트 신고")
                .reportedAt(now)
                .status(ReportStatus.PENDING)
                .build();
        report.setCategory(category);

        // when
        ReportResponse response = ReportResponse.from(report);

        // then
        assertThat(response.getReportId()).isEqualTo("REPORT-001");
        assertThat(response.getReporterId()).isEqualTo("USER-001");
        assertThat(response.getReportedId()).isEqualTo("USER-002");
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(response.getReportCategory()).isEqualTo("SPAM");
        assertThat(response.getReason()).isEqualTo("테스트 신고");
        assertThat(response.getReportedAt()).isEqualTo(now);
        assertThat(response.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("Builder 테스트")
    void builder_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        ReportResponse response = ReportResponse.builder()
                .reportId("REPORT-002")
                .reporterId("USER-003")
                .reportedId("USER-004")
                .referenceType(ReferenceType.ARTICLE)
                .reportCategory("ABUSE")
                .reason("빌더 테스트")
                .reportedAt(now)
                .status(ReportStatus.REVIEWING)
                .build();

        // then
        assertThat(response.getReportId()).isEqualTo("REPORT-002");
        assertThat(response.getReporterId()).isEqualTo("USER-003");
        assertThat(response.getReportedId()).isEqualTo("USER-004");
        assertThat(response.getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(response.getReportCategory()).isEqualTo("ABUSE");
        assertThat(response.getReason()).isEqualTo("빌더 테스트");
        assertThat(response.getReportedAt()).isEqualTo(now);
        assertThat(response.getStatus()).isEqualTo(ReportStatus.REVIEWING);
    }

    @Test
    @DisplayName("다양한 상태로 변환 테스트")
    void from_DifferentStatuses() {
        // given
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.PROFILE, "SPAM"))
                .build();

        ReportStatus[] statuses = ReportStatus.values();

        for (ReportStatus status : statuses) {
            Report report = Report.builder()
                    .reportId("REPORT-" + status.name())
                    .reporterId("USER-001")
                    .reportedId("USER-002")
                    .reason("상태 테스트: " + status.name())
                    .reportedAt(LocalDateTime.now())
                    .status(status)
                    .build();
            report.setCategory(category);

            // when
            ReportResponse response = ReportResponse.from(report);

            // then
            assertThat(response.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("다양한 ReferenceType으로 변환 테스트")
    void from_DifferentReferenceTypes() {
        // given
        ReferenceType[] types = ReferenceType.values();

        for (ReferenceType type : types) {
            ReportCategory category = ReportCategory.builder()
                    .id(new ReportCategoryId(type, "TEST"))
                    .build();

            Report report = Report.builder()
                    .reportId("REPORT-" + type.name())
                    .reporterId("USER-001")
                    .reportedId("USER-002")
                    .reason("타입 테스트: " + type.name())
                    .reportedAt(LocalDateTime.now())
                    .status(ReportStatus.PENDING)
                    .build();
            report.setCategory(category);

            // when
            ReportResponse response = ReportResponse.from(report);

            // then
            assertThat(response.getReferenceType()).isEqualTo(type);
        }
    }
}
