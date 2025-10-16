package com.teambind.supportserver.report.utils;

import com.teambind.supportserver.common.utils.IdGenerator;
import com.teambind.supportserver.common.utils.Snowflake;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeTest {

    @Test
    @DisplayName("generateLongId는 버스트 상황에서도 양수·증가·유일 ID를 반환한다")
    void generateLongId_shouldReturnUniqueIncreasingPositiveIds() {
        Snowflake generator = new Snowflake();
        int count = 10_000; // burst generation to hit same-millis sequence behavior
        long prev = -1;
        Set<Long> seen = new HashSet<>(count * 2);

        for (int i = 0; i < count; i++) {
            long id = generator.generateLongId();
            assertTrue(id > 0, "ID should be positive");
            if (prev != -1) {
                assertTrue(id > prev, "IDs must be strictly increasing");
            }
            assertTrue(seen.add(id), "IDs must be unique");
            prev = id;
        }
    }

    @RepeatedTest(3)
    @DisplayName("generateId는 파싱 가능한 양의 숫자 문자열을 반환한다")
    void generateId_shouldReturnParsablePositiveString() {
        IdGenerator generator = new Snowflake();
        String idStr = generator.generateId();
        assertNotNull(idStr);
        assertFalse(idStr.isBlank());
        assertTrue(idStr.matches("\\d+"), "ID string should be numeric");
        long parsed = Long.parseLong(idStr);
        assertTrue(parsed > 0);
    }
}
