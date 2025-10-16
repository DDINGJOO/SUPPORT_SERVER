package com.teambind.supportserver.report.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ReportExceptionTest {

    @Test
    @DisplayName("ReportException은 상태, 코드, 메시지를 올바르게 노출한다")
    void reportException_properties() {
        ErrorCode code = ErrorCode.REPORT_NOT_FOUND;
        ReportException ex = new ReportException(code);

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(code, ex.getErrorCode());
        assertEquals(code.toString(), ex.getMessage());
    }
}
