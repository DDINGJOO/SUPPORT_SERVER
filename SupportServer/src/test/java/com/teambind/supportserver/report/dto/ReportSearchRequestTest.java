package com.teambind.supportserver.report.dto;

import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportSearchRequest DTO 테스트
 */
@DisplayName("ReportSearchRequest DTO 테스트")
class ReportSearchRequestTest {

    @Test
    @DisplayName("기본값 테스트 - sortType")
    void defaultValue_SortType() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();

        // when
        ReportSearchRequest.SortType sortType = request.getSortType();

        // then
        assertThat(sortType).isEqualTo(ReportSearchRequest.SortType.REPORTED_AT);
    }

    @Test
    @DisplayName("기본값 테스트 - sortDirection")
    void defaultValue_SortDirection() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();

        // when
        ReportSearchRequest.SortDirection sortDirection = request.getSortDirection();

        // then
        assertThat(sortDirection).isEqualTo(ReportSearchRequest.SortDirection.DESC);
    }

    @Test
    @DisplayName("기본값 테스트 - size (기본값 20)")
    void defaultValue_Size() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder().build();

        // when
        Integer size = request.getSize();

        // then
        assertThat(size).isEqualTo(20);
    }

    @Test
    @DisplayName("size 최대값 제한 - 100 초과시 100으로 제한")
    void maxSize_LimitTo100() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .size(150)
                .build();

        // when
        Integer size = request.getSize();

        // then
        assertThat(size).isEqualTo(100);
    }

    @Test
    @DisplayName("size 최소값 제한 - 0 이하시 기본값 20")
    void minSize_DefaultTo20() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .size(0)
                .build();

        // when
        Integer size = request.getSize();

        // then
        assertThat(size).isEqualTo(20);
    }

    @Test
    @DisplayName("size 음수값 제한 - 기본값 20")
    void negativeSize_DefaultTo20() {
        // given
        ReportSearchRequest request = ReportSearchRequest.builder()
                .size(-10)
                .build();

        // when
        Integer size = request.getSize();

        // then
        assertThat(size).isEqualTo(20);
    }

    @Test
    @DisplayName("모든 필터 설정 테스트")
    void allFilters_Set() {
        // given & when
        ReportSearchRequest request = ReportSearchRequest.builder()
                .status(ReportStatus.PENDING)
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("SPAM")
                .sortType(ReportSearchRequest.SortType.STATUS)
                .sortDirection(ReportSearchRequest.SortDirection.ASC)
                .cursor("cursor-value")
                .size(50)
                .build();

        // then
        assertThat(request.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(request.getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(request.getReportCategory()).isEqualTo("SPAM");
        assertThat(request.getSortType()).isEqualTo(ReportSearchRequest.SortType.STATUS);
        assertThat(request.getSortDirection()).isEqualTo(ReportSearchRequest.SortDirection.ASC);
        assertThat(request.getCursor()).isEqualTo("cursor-value");
        assertThat(request.getSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("null 필터 테스트")
    void nullFilters() {
        // given & when
        ReportSearchRequest request = ReportSearchRequest.builder()
                .status(null)
                .referenceType(null)
                .reportCategory(null)
                .cursor(null)
                .build();

        // then
        assertThat(request.getStatus()).isNull();
        assertThat(request.getReferenceType()).isNull();
        assertThat(request.getReportCategory()).isNull();
        assertThat(request.getCursor()).isNull();
    }

    @Test
    @DisplayName("SortType enum 값 확인")
    void sortType_EnumValues() {
        // when & then
        assertThat(ReportSearchRequest.SortType.values()).hasSize(2);
        assertThat(ReportSearchRequest.SortType.values())
                .contains(
                        ReportSearchRequest.SortType.STATUS,
                        ReportSearchRequest.SortType.REPORTED_AT
                );
    }

    @Test
    @DisplayName("SortDirection enum 값 확인")
    void sortDirection_EnumValues() {
        // when & then
        assertThat(ReportSearchRequest.SortDirection.values()).hasSize(2);
        assertThat(ReportSearchRequest.SortDirection.values())
                .contains(
                        ReportSearchRequest.SortDirection.ASC,
                        ReportSearchRequest.SortDirection.DESC
                );
    }
}
