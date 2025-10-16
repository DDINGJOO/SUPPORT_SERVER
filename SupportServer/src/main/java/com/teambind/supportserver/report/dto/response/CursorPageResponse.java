package com.teambind.supportserver.report.dto.response;

import lombok.*;

import java.util.List;

/**
 * 커서 기반 페이징 응답 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {

    private List<T> content;        // 조회된 데이터 목록
    private String nextCursor;      // 다음 페이지를 위한 커서 (null이면 마지막 페이지)
    private Integer size;           // 요청한 페이지 크기
    private Boolean hasNext;        // 다음 페이지 존재 여부

    /**
     * 다음 페이지가 있는 응답 생성
     */
    public static <T> CursorPageResponse<T> of(List<T> content, String nextCursor, Integer size) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .size(size)
                .hasNext(nextCursor != null)
                .build();
    }

    /**
     * 마지막 페이지 응답 생성
     */
    public static <T> CursorPageResponse<T> last(List<T> content, Integer size) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(null)
                .size(size)
                .hasNext(false)
                .build();
    }
}
