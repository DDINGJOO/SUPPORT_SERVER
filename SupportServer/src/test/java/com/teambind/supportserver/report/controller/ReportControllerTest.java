package com.teambind.supportserver.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.supportserver.report.dto.request.ReportRequest;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.dto.request.ReportStatusUpdateRequest;
import com.teambind.supportserver.report.dto.response.CursorPageResponse;
import com.teambind.supportserver.report.dto.response.ReportResponse;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import com.teambind.supportserver.report.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("신고 등록 - 성공")
    void createReport_Success() throws Exception {
        // Given
        ReportRequest request = ReportRequest.builder()
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("harassment")
                .reason("욕설 및 비방")
                .build();

        ReportResponse response = ReportResponse.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("harassment")
                .reason("욕설 및 비방")
                .status(ReportStatus.PENDING)
                .reportedAt(LocalDateTime.now())
                .build();

        given(reportService.createReport(anyString(), anyString(), any(), anyString(), anyString()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value("REPORT-001"))
                .andExpect(jsonPath("$.reporterId").value("USER-001"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(reportService, times(1)).createReport(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("신고 등록 - 유효성 검사 실패 (신고자 ID 누락)")
    void createReport_ValidationFail_MissingReporterId() throws Exception {
        // Given
        ReportRequest request = ReportRequest.builder()
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("harassment")
                .reason("욕설 및 비방")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reportService, never()).createReport(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("신고 등록 - 유효성 검사 실패 (신고 사유 너무 김)")
    void createReport_ValidationFail_ReasonTooLong() throws Exception {
        // Given
        String longReason = "A".repeat(501);
        ReportRequest request = ReportRequest.builder()
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("harassment")
                .reason(longReason)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reportService, never()).createReport(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("신고 상세 조회 - 성공")
    void getReport_Success() throws Exception {
        // Given
        String reportId = "REPORT-001";
        ReportResponse response = ReportResponse.builder()
                .reportId(reportId)
                .reporterId("USER-001")
                .reportedId("USER-002")
                .referenceType(ReferenceType.PROFILE)
                .reportCategory("harassment")
                .reason("욕설 및 비방")
                .status(ReportStatus.PENDING)
                .reportedAt(LocalDateTime.now())
                .build();

        given(reportService.getReport(reportId)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/{reportId}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(reportService, times(1)).getReport(reportId);
    }

    @Test
    @DisplayName("신고 목록 검색 - 성공")
    void searchReports_Success() throws Exception {
        // Given
        ReportResponse report1 = ReportResponse.builder()
                .reportId("REPORT-001")
                .reporterId("USER-001")
                .status(ReportStatus.PENDING)
                .build();

        ReportResponse report2 = ReportResponse.builder()
                .reportId("REPORT-002")
                .reporterId("USER-002")
                .status(ReportStatus.PENDING)
                .build();

        CursorPageResponse<ReportResponse> pageResponse = CursorPageResponse.of(
                List.of(report1, report2),
                "cursor-123",
                20
        );

        given(reportService.searchReports(any(ReportSearchRequest.class))).willReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/reports")
                        .param("status", "PENDING")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("cursor-123"));

        verify(reportService, times(1)).searchReports(any(ReportSearchRequest.class));
    }

    @Test
    @DisplayName("신고 상태 변경 - 성공")
    void updateReportStatus_Success() throws Exception {
        // Given
        String reportId = "REPORT-001";
        ReportStatusUpdateRequest request = ReportStatusUpdateRequest.builder()
                .status(ReportStatus.APPROVED)
                .adminId("ADMIN-001")
                .comment("승인 처리")
                .build();

        ReportResponse response = ReportResponse.builder()
                .reportId(reportId)
                .status(ReportStatus.APPROVED)
                .build();

        given(reportService.getReport(reportId)).willReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(reportService, times(1)).updateReportStatus(eq(reportId), eq(ReportStatus.APPROVED), eq("ADMIN-001"), anyString());
        verify(reportService, times(1)).getReport(reportId);
    }

    @Test
    @DisplayName("신고 상태 변경 - 유효성 검사 실패 (상태 누락)")
    void updateReportStatus_ValidationFail_MissingStatus() throws Exception {
        // Given
        String reportId = "REPORT-001";
        ReportStatusUpdateRequest request = ReportStatusUpdateRequest.builder()
                .adminId("ADMIN-001")
                .comment("승인 처리")
                .build();

        // When & Then
        mockMvc.perform(patch("/api/v1/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reportService, never()).updateReportStatus(anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("신고 철회 - 성공")
    void withdrawReport_Success() throws Exception {
        // Given
        String reportId = "REPORT-001";
        ReportController.WithdrawRequest request = new ReportController.WithdrawRequest("USER-001", "실수로 신고함");

        ReportResponse response = ReportResponse.builder()
                .reportId(reportId)
                .status(ReportStatus.WITHDRAWN)
                .build();

        given(reportService.getReport(reportId)).willReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));

        verify(reportService, times(1)).withdrawReport(reportId, "USER-001", "실수로 신고함");
        verify(reportService, times(1)).getReport(reportId);
    }
}
