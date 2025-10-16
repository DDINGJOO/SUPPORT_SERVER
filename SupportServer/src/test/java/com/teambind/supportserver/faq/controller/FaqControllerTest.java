package com.teambind.supportserver.faq.controller;

import com.teambind.supportserver.faq.entity.Faq;
import com.teambind.supportserver.faq.entity.enums.FaqCategory;
import com.teambind.supportserver.faq.service.FaqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FaqController 단위 테스트
 */
@WebMvcTest(FaqController.class)
@DisplayName("FaqController 단위 테스트")
class FaqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaqService faqService;

    @Test
    @DisplayName("FAQ 목록 조회 - 카테고리 파라미터 없이 (기본값 ALL)")
    void getFaqs_WithoutCategoryParam() throws Exception {
        // given
        List<Faq> faqs = createTestFaqs();
        given(faqService.getFaqsByCategory(FaqCategory.ALL)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].category").value("RESERVATION"))
                .andExpect(jsonPath("$[0].question").value("예약은 어떻게 하나요?"));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.ALL);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - ALL 카테고리로 조회")
    void getFaqs_WithAllCategory() throws Exception {
        // given
        List<Faq> faqs = createTestFaqs();
        given(faqService.getFaqsByCategory(FaqCategory.ALL)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.ALL);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - RESERVATION 카테고리로 필터링")
    void getFaqs_WithReservationCategory() throws Exception {
        // given
        List<Faq> faqs = List.of(createFaq(1L, FaqCategory.RESERVATION, "예약은 어떻게 하나요?", "앱에서 예약 가능합니다."));
        given(faqService.getFaqsByCategory(FaqCategory.RESERVATION)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "RESERVATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("RESERVATION"))
                .andExpect(jsonPath("$[0].question").value("예약은 어떻게 하나요?"));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.RESERVATION);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - CHECK_IN 카테고리로 필터링")
    void getFaqs_WithCheckInCategory() throws Exception {
        // given
        List<Faq> faqs = List.of(createFaq(2L, FaqCategory.CHECK_IN, "체크인 시간은 언제인가요?", "오후 3시부터 가능합니다."));
        given(faqService.getFaqsByCategory(FaqCategory.CHECK_IN)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "CHECK_IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("CHECK_IN"));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.CHECK_IN);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - PAYMENT 카테고리로 필터링")
    void getFaqs_WithPaymentCategory() throws Exception {
        // given
        List<Faq> faqs = List.of(createFaq(3L, FaqCategory.PAYMENT, "결제 수단은 무엇이 있나요?", "카드와 계좌이체가 가능합니다."));
        given(faqService.getFaqsByCategory(FaqCategory.PAYMENT)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "PAYMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("PAYMENT"));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.PAYMENT);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - REVIEW_REPORT 카테고리로 필터링")
    void getFaqs_WithReviewReportCategory() throws Exception {
        // given
        List<Faq> faqs = List.of(createFaq(4L, FaqCategory.REVIEW_REPORT, "리뷰는 어떻게 작성하나요?", "이용 후 앱에서 작성 가능합니다."));
        given(faqService.getFaqsByCategory(FaqCategory.REVIEW_REPORT)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "REVIEW_REPORT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("REVIEW_REPORT"));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.REVIEW_REPORT);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - ETC 카테고리로 필터링")
    void getFaqs_WithEtcCategory() throws Exception {
        // given
        List<Faq> faqs = List.of(createFaq(5L, FaqCategory.ETC, "문의는 어디로 하나요?", "고객센터로 문의해주세요."));
        given(faqService.getFaqsByCategory(FaqCategory.ETC)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "ETC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("ETC"));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.ETC);
    }

    @Test
    @DisplayName("FAQ 목록 조회 - 빈 목록 반환")
    void getFaqs_EmptyList() throws Exception {
        // given
        given(faqService.getFaqsByCategory(any())).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "PAYMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.PAYMENT);
    }

    @Test
    @DisplayName("FAQ 캐시 수동 갱신 - 성공")
    void refreshCache_Success() throws Exception {
        // given
        doNothing().when(faqService).refreshCache();

        // when & then
        mockMvc.perform(post("/api/v1/faqs/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("FAQ cache refreshed successfully"));

        verify(faqService, times(1)).refreshCache();
    }

    @Test
    @DisplayName("FAQ 목록 조회 - 잘못된 카테고리 파라미터")
    void getFaqs_WithInvalidCategory() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "INVALID_CATEGORY"))
                .andExpect(status().isBadRequest());

        verify(faqService, never()).getFaqsByCategory(any());
    }

    @Test
    @DisplayName("FAQ 목록 조회 - 응답 필드 검증")
    void getFaqs_ResponseFieldValidation() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Faq> faqs = List.of(
                Faq.builder()
                        .id(1L)
                        .category(FaqCategory.RESERVATION)
                        .question("예약은 어떻게 하나요?")
                        .answer("앱에서 예약 가능합니다.")
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
        given(faqService.getFaqsByCategory(FaqCategory.RESERVATION)).willReturn(faqs);

        // when & then
        mockMvc.perform(get("/api/v1/faqs")
                        .param("category", "RESERVATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].question").exists())
                .andExpect(jsonPath("$[0].answer").exists())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());

        verify(faqService, times(1)).getFaqsByCategory(FaqCategory.RESERVATION);
    }

    // === 테스트 데이터 생성 헬퍼 메서드 ===

    private List<Faq> createTestFaqs() {
        return List.of(
                createFaq(1L, FaqCategory.RESERVATION, "예약은 어떻게 하나요?", "앱에서 예약 가능합니다."),
                createFaq(2L, FaqCategory.CHECK_IN, "체크인 시간은 언제인가요?", "오후 3시부터 가능합니다."),
                createFaq(3L, FaqCategory.PAYMENT, "결제 수단은 무엇이 있나요?", "카드와 계좌이체가 가능합니다."),
                createFaq(4L, FaqCategory.REVIEW_REPORT, "리뷰는 어떻게 작성하나요?", "이용 후 앱에서 작성 가능합니다."),
                createFaq(5L, FaqCategory.ETC, "문의는 어디로 하나요?", "고객센터로 문의해주세요.")
        );
    }

    private Faq createFaq(Long id, FaqCategory category, String question, String answer) {
        return Faq.builder()
                .id(id)
                .category(category)
                .question(question)
                .answer(answer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
