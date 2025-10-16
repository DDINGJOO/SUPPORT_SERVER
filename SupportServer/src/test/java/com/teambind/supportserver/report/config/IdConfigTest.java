package com.teambind.supportserver.report.config;

import com.teambind.supportserver.common.config.IdConfig;
import com.teambind.supportserver.common.utils.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdConfigTest {

    @Test
    @DisplayName("IdConfig provides an IdGenerator bean capable of generating valid IDs")
    void idConfig_providesWorkingIdGenerator() {
        IdConfig config = new IdConfig();
        IdGenerator generator = config.idGenerator();
        assertNotNull(generator);

        String idStr = generator.generateId();
        Long idLong = generator.generateLongId();

        assertNotNull(idStr);
        assertTrue(idStr.matches("\\d+"));
        assertTrue(Long.parseLong(idStr) > 0);
        assertNotNull(idLong);
        assertTrue(idLong > 0);
    }
}
