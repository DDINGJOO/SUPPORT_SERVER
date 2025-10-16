package com.teambind.supportserver.report.dto.request;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import lombok.*;

/**
 * 신고 검색 요청 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSearchRequest {

    // 필터 조건
    private ReportStatus status;           // 신고 상태 필터
    private ReferenceType referenceType;   // 대상 타입 필터 (PROFILE, ARTICLE, BUSINESS)
    private String reportCategory;         // 신고 카테고리 필터

    // 정렬 조건
    private SortType sortType;             // 정렬 기준
    private SortDirection sortDirection;   // 정렬 방향

    // 커서 페이징
    private String cursor;                 // 커서 (마지막 항목의 ID 또는 정렬 기준 값)
    private Integer size;                  // 페이지 크기 (기본값: 20)

    public enum SortType {
        STATUS,      // 상태 기준 정렬
        REPORTED_AT  // 신고일 기준 정렬
    }

    public enum SortDirection {
        ASC,   // 오름차순
        DESC   // 내림차순
    }

    // 기본값 설정
    public SortType getSortType() {
        return sortType != null ? sortType : SortType.REPORTED_AT;
    }

    public SortDirection getSortDirection() {
        return sortDirection != null ? sortDirection : SortDirection.DESC;
    }

    public Integer getSize() {
        return size != null && size > 0 ? Math.min(size, 100) : 20;
    }
}
