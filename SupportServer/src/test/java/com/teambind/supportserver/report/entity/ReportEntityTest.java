package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Report 엔티티 단위 테스트
 */
@DisplayName("Report 엔티티 테스트")
class ReportEntityTest {

    @Test
    @DisplayName("Report 엔티티 생성 - 정상")
    void createReport_Success() {
        // given
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.PROFILE, "SPAM"))
                .build();

        // when
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("스팸 계정입니다")
                .reportedAt(LocalDateTime.now())
                .build();

        report.setCategory(category);

        // then
        assertThat(report).isNotNull();
        assertThat(report.getReportId()).isEqualTo("REPORT-001");
        assertThat(report.getReporterId()).isEqualTo("USER-001");
        assertThat(report.getReportedId()).isEqualTo("USER-002");
        assertThat(report.getReason()).isEqualTo("스팸 계정입니다");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getCategory()).isEqualTo(category);
        assertThat(report.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(report.getReportCategory()).isEqualTo("SPAM");
    }

    @Test
    @DisplayName("Report 엔티티 생성 - 기본 상태는 PENDING")
    void createReport_DefaultStatus() {
        // when
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("부적절한 콘텐츠")
                .build();

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("신고 승인 - 상태가 APPROVED로 변경")
    void approveReport_StatusChangedToApproved() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("욕설 사용")
                .status(ReportStatus.PENDING)
                .build();

        // when
        report.approve();

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("신고 거부 - 상태가 REJECTED로 변경")
    void rejectReport_StatusChangedToRejected() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("허위 신고")
                .status(ReportStatus.PENDING)
                .build();

        // when
        report.reject();

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
    }

    @Test
    @DisplayName("신고 철회 - 상태가 WITHDRAWN으로 변경")
    void withdrawReport_StatusChangedToWithdrawn() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("실수로 신고함")
                .status(ReportStatus.PENDING)
                .build();

        // when
        report.withdraw();

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("검토 시작 - 상태가 REVIEWING으로 변경")
    void startReview_StatusChangedToReviewing() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("검토 필요")
                .status(ReportStatus.PENDING)
                .build();

        // when
        report.startReview();

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REVIEWING);
    }

    @Test
    @DisplayName("isPending - PENDING 상태일 때 true 반환")
    void isPending_WhenStatusIsPending_ReturnsTrue() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("대기 중")
                .status(ReportStatus.PENDING)
                .build();

        // when & then
        assertThat(report.isPending()).isTrue();
    }

    @Test
    @DisplayName("isPending - PENDING 상태가 아닐 때 false 반환")
    void isPending_WhenStatusIsNotPending_ReturnsFalse() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("검토 중")
                .status(ReportStatus.REVIEWING)
                .build();

        // when & then
        assertThat(report.isPending()).isFalse();
    }

    @Test
    @DisplayName("isApproved - APPROVED 상태일 때 true 반환")
    void isApproved_WhenStatusIsApproved_ReturnsTrue() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("승인됨")
                .status(ReportStatus.APPROVED)
                .build();

        // when & then
        assertThat(report.isApproved()).isTrue();
    }

    @Test
    @DisplayName("changeStatus - 상태 변경 성공")
    void changeStatus_Success() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("상태 변경 테스트")
                .status(ReportStatus.PENDING)
                .build();

        // when
        report.changeStatus(ReportStatus.REVIEWING);

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REVIEWING);
    }

    @Test
    @DisplayName("setCategory - 카테고리 설정 시 referenceType과 reportCategory 자동 설정")
    void setCategory_AutoSetReferenceTypeAndReportCategory() {
        // given
        ReportCategory category = ReportCategory.builder()
                .id(new ReportCategoryId(ReferenceType.ARTICLE, "INAPPROPRIATE"))
                .build();

        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("부적절한 게시글")
                .build();

        // when
        report.setCategory(category);

        // then
        assertThat(report.getCategory()).isEqualTo(category);
        assertThat(report.getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(report.getReportCategory()).isEqualTo("INAPPROPRIATE");
    }

    @Test
    @DisplayName("setCategory - null 카테고리 설정 시 referenceType과 reportCategory는 null")
    void setCategory_WithNullCategory_NoAutoSet() {
        // given
        Report report = Report.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .reason("카테고리 없음")
                .build();

        // when
        report.setCategory(null);

        // then
        assertThat(report.getCategory()).isNull();
        assertThat(report.getReferenceType()).isNull();
        assertThat(report.getReportCategory()).isNull();
    }
}
