package com.teambind.supportserver.faq.controller;

import com.teambind.supportserver.faq.entity.Faq;
import com.teambind.supportserver.faq.entity.enums.FaqCategory;
import com.teambind.supportserver.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FAQ 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    /**
     * FAQ 목록 조회 (카테고리 필터링 가능)
     *
     * @param category FAQ 카테고리 (선택, 기본값: ALL)
     * @return FAQ 목록
     */
    @GetMapping
    public ResponseEntity<List<Faq>> getFaqs(
            @RequestParam(required = false, defaultValue = "ALL") FaqCategory category) {
        log.info("Fetching FAQs: category={}", category);

        List<Faq> faqs = faqService.getFaqsByCategory(category);

        log.info("Returned {} FAQs", faqs.size());

        return ResponseEntity.ok(faqs);
    }

    /**
     * FAQ 캐시 수동 갱신 (관리자용)
     *
     * @return 갱신 결과
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshCache() {
        log.info("Manual FAQ cache refresh requested");

        faqService.refreshCache();

        log.info("FAQ cache refreshed successfully");

        return ResponseEntity.ok("FAQ cache refreshed successfully");
    }
}
