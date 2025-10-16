package com.teambind.supportserver.report.dto;

import com.teambind.supportserver.report.dto.response.CursorPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CursorPageResponse DTO 테스트
 */
@DisplayName("CursorPageResponse DTO 테스트")
class CursorPageResponseTest {

    @Test
    @DisplayName("of() - 다음 페이지 있음")
    void of_HasNextPage() {
        // given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        String nextCursor = "cursor-123";
        Integer size = 3;

        // when
        CursorPageResponse<String> response = CursorPageResponse.of(content, nextCursor, size);

        // then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getNextCursor()).isEqualTo("cursor-123");
        assertThat(response.getSize()).isEqualTo(3);
        assertThat(response.getHasNext()).isTrue();
    }

    @Test
    @DisplayName("last() - 마지막 페이지")
    void last_NoNextPage() {
        // given
        List<String> content = Arrays.asList("item1", "item2");
        Integer size = 5;

        // when
        CursorPageResponse<String> response = CursorPageResponse.last(content, size);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getHasNext()).isFalse();
    }

    @Test
    @DisplayName("빈 컨텐츠 테스트")
    void emptyContent() {
        // given
        List<String> content = Collections.emptyList();

        // when
        CursorPageResponse<String> response = CursorPageResponse.last(content, 10);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("제네릭 타입 테스트 - Integer")
    void genericType_Integer() {
        // given
        List<Integer> content = Arrays.asList(1, 2, 3, 4, 5);
        String nextCursor = "cursor-456";

        // when
        CursorPageResponse<Integer> response = CursorPageResponse.of(content, nextCursor, 5);

        // then
        assertThat(response.getContent()).containsExactly(1, 2, 3, 4, 5);
        assertThat(response.getHasNext()).isTrue();
    }

    @Test
    @DisplayName("제네릭 타입 테스트 - Custom Object")
    void genericType_CustomObject() {
        // given
        List<TestDto> content = Arrays.asList(
                new TestDto("A", 1),
                new TestDto("B", 2)
        );

        // when
        CursorPageResponse<TestDto> response = CursorPageResponse.last(content, 10);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).name).isEqualTo("A");
        assertThat(response.getContent().get(1).value).isEqualTo(2);
    }

    @Test
    @DisplayName("Builder 테스트")
    void builder_Success() {
        // given & when
        CursorPageResponse<String> response = CursorPageResponse.<String>builder()
                .content(Arrays.asList("a", "b", "c"))
                .nextCursor("next-cursor")
                .size(3)
                .hasNext(true)
                .build();

        // then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getNextCursor()).isEqualTo("next-cursor");
        assertThat(response.getSize()).isEqualTo(3);
        assertThat(response.getHasNext()).isTrue();
    }

    @Test
    @DisplayName("nextCursor null 테스트")
    void nextCursor_Null() {
        // given
        List<String> content = Arrays.asList("item1");

        // when
        CursorPageResponse<String> response = CursorPageResponse.of(content, null, 1);

        // then
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getHasNext()).isFalse();
    }

    @Test
    @DisplayName("대량 데이터 테스트")
    void largeContent() {
        // given
        List<Integer> content = Arrays.asList(new Integer[100]);
        String nextCursor = "large-cursor";

        // when
        CursorPageResponse<Integer> response = CursorPageResponse.of(content, nextCursor, 100);

        // then
        assertThat(response.getContent()).hasSize(100);
        assertThat(response.getHasNext()).isTrue();
    }

    // 테스트용 DTO
    private static class TestDto {
        String name;
        Integer value;

        TestDto(String name, Integer value) {
            this.name = name;
            this.value = value;
        }
    }
}
