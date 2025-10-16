package com.teambind.supportserver.faq.service;

import com.teambind.supportserver.faq.entity.Faq;
import com.teambind.supportserver.faq.entity.enums.FaqCategory;
import com.teambind.supportserver.faq.repository.FaqRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * FaqService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FaqService 단위 테스트")
class FaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private FaqService faqService;

    private List<Faq> testFaqs;

    @BeforeEach
    void setUp() {
        testFaqs = createTestFaqs();
    }

    @Test
    @DisplayName("서버 시작 시 캐시 초기화 성공")
    void init_Success() {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);

        // when
        faqService.init();

        // then
        List<Faq> result = faqService.getAllFaqs();
        assertThat(result).hasSize(5);
        verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("모든 FAQ 조회 성공")
    void getAllFaqs_Success() {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);
        faqService.refreshCache();

        // when
        List<Faq> result = faqService.getAllFaqs();

        // then
        assertThat(result).hasSize(5);
        assertThat(result).extracting(Faq::getCategory)
                .containsExactlyInAnyOrder(
                        FaqCategory.RESERVATION,
                        FaqCategory.CHECK_IN,
                        FaqCategory.PAYMENT,
                        FaqCategory.REVIEW_REPORT,
                        FaqCategory.ETC
                );
    }

    @Test
    @DisplayName("카테고리별 FAQ 조회 - ALL 카테고리")
    void getFaqsByCategory_All() {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);
        faqService.refreshCache();

        // when
        List<Faq> result = faqService.getFaqsByCategory(FaqCategory.ALL);

        // then
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("카테고리별 FAQ 조회 - 특정 카테고리 (RESERVATION)")
    void getFaqsByCategory_Reservation() {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);
        faqService.refreshCache();

        // when
        List<Faq> result = faqService.getFaqsByCategory(FaqCategory.RESERVATION);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(FaqCategory.RESERVATION);
        assertThat(result.get(0).getQuestion()).contains("예약");
    }

    @Test
    @DisplayName("카테고리별 FAQ 조회 - 특정 카테고리 (PAYMENT)")
    void getFaqsByCategory_Payment() {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);
        faqService.refreshCache();

        // when
        List<Faq> result = faqService.getFaqsByCategory(FaqCategory.PAYMENT);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(FaqCategory.PAYMENT);
        assertThat(result.get(0).getQuestion()).contains("결제");
    }

    @Test
    @DisplayName("캐시 갱신 성공")
    void refreshCache_Success() {
        // given
        List<Faq> newFaqs = List.of(
                createFaq(1L, FaqCategory.RESERVATION, "새 질문1", "새 답변1"),
                createFaq(2L, FaqCategory.PAYMENT, "새 질문2", "새 답변2")
        );
        given(faqRepository.findAll()).willReturn(testFaqs).willReturn(newFaqs);

        faqService.refreshCache();
        assertThat(faqService.getAllFaqs()).hasSize(5);

        // when
        faqService.refreshCache();

        // then
        List<Faq> result = faqService.getAllFaqs();
        assertThat(result).hasSize(2);
        verify(faqRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 스레드에서 동시 조회")
    void concurrentRead_Success() throws InterruptedException {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);
        faqService.refreshCache();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    FaqCategory category = FaqCategory.values()[finalI % FaqCategory.values().length];
                    List<Faq> result = faqService.getFaqsByCategory(category);
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시성 테스트 - 조회 중 캐시 갱신")
    void concurrentReadAndRefresh_Success() throws InterruptedException {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);
        faqService.refreshCache();

        int readThreadCount = 50;
        int refreshThreadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(readThreadCount + refreshThreadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when - 읽기 스레드
        for (int i = 0; i < readThreadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    FaqCategory category = FaqCategory.values()[finalI % FaqCategory.values().length];
                    List<Faq> result = faqService.getFaqsByCategory(category);
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // when - 갱신 스레드
        for (int i = 0; i < refreshThreadCount; i++) {
            executorService.submit(() -> {
                try {
                    faqService.refreshCache();
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(readThreadCount + refreshThreadCount);
    }

    @Test
    @DisplayName("스케줄러 메서드 호출 성공")
    void scheduledRefreshCache_Success() {
        // given
        given(faqRepository.findAll()).willReturn(testFaqs);

        // when
        faqService.scheduledRefreshCache();

        // then
        List<Faq> result = faqService.getAllFaqs();
        assertThat(result).hasSize(5);
        verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("빈 FAQ 목록 조회")
    void getFaqs_EmptyList() {
        // given
        given(faqRepository.findAll()).willReturn(new ArrayList<>());
        faqService.refreshCache();

        // when
        List<Faq> result = faqService.getAllFaqs();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 조회 시 빈 목록 반환")
    void getFaqsByCategory_NonExistentCategory() {
        // given
        List<Faq> onlyReservationFaqs = List.of(
                createFaq(1L, FaqCategory.RESERVATION, "예약 질문", "예약 답변")
        );
        given(faqRepository.findAll()).willReturn(onlyReservationFaqs);
        faqService.refreshCache();

        // when
        List<Faq> result = faqService.getFaqsByCategory(FaqCategory.PAYMENT);

        // then
        assertThat(result).isEmpty();
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
