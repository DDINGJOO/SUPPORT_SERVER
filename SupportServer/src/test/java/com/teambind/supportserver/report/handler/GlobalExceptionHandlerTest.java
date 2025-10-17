package com.teambind.supportserver.report.handler;

import com.teambind.supportserver.common.handler.GlobalExceptionHandler;
import com.teambind.supportserver.report.exceptions.ErrorCode;
import com.teambind.supportserver.report.exceptions.ReportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("GlobalExceptionHandler는 ReportException을 상태와 메시지를 담은 ResponseEntity로 매핑한다")
    void handleCustomException_shouldReturnProperResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ReportException ex = new ReportException(ErrorCode.REPORT_NOT_FOUND);

        ResponseEntity<?> response = handler.handleCustomException(ex);

        assertEquals(ex.getStatus(), response.getStatusCode());
        assertEquals(ex.getMessage(), response.getBody());
    }
}
