package com.teambind.supportserver.faq.service;

import com.teambind.supportserver.faq.entity.Faq;
import com.teambind.supportserver.faq.entity.enums.FaqCategory;
import com.teambind.supportserver.faq.repository.FaqRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * FAQ 서비스
 *
 * 스케일 아웃 환경 고려사항:
 *
 *   각 서버 인스턴스가 독립적인 로컬 캐시를 유지
 *   스케줄러가 각 인스턴스에서 독립적으로 실행되어 캐시 갱신 (읽기 전용이므로 문제없음)
 *   ReadWriteLock은 동일 서버 내 동시성 제어용 (멀티스레드 환경)
 *   분산 락 불필요 - 각 인스턴스가 DB에서 독립적으로 읽기만 수행
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    // 로컬 서버 인스턴스 내 멀티스레드 동시성 제어용
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // 로컬 캐시 (각 서버 인스턴스마다 독립적으로 유지)
    private volatile List<Faq> cachedFaqs = new ArrayList<>();

    /**
     * 서버 시작 시 FAQ 데이터 캐싱
     */
    @PostConstruct
    public void init() {
        log.info("Initializing FAQ cache...");
        refreshCache();
        log.info("FAQ cache initialized with {} items", cachedFaqs.size());
    }

    /**
     * 매일 새벽 3시에 캐시 갱신
     *
     * <p>스케일 아웃 환경에서 각 인스턴스가 독립적으로 실행되며,
     * DB에서 읽기만 수행하므로 분산 락 불필요</p>
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledRefreshCache() {
        log.info("Scheduled FAQ cache refresh started");
        refreshCache();
        log.info("Scheduled FAQ cache refresh completed with {} items", cachedFaqs.size());
    }

    /**
     * 캐시 갱신
     *
     * <p>쓰기 락으로 동일 서버 내 멀티스레드 동시 갱신 방지</p>
     * <p>DB 읽기만 수행하므로 분산 환경에서도 안전</p>
     */
    @Transactional(readOnly = true)
    public void refreshCache() {
        lock.writeLock().lock();
        try {
            List<Faq> newCache = faqRepository.findAll();
            cachedFaqs = newCache; // volatile 변수에 원자적 할당
            log.debug("FAQ cache refreshed: {} items loaded", cachedFaqs.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 모든 FAQ 조회
     *
     * @return FAQ 목록
     */
    public List<Faq> getAllFaqs() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(cachedFaqs);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 카테고리별 FAQ 조회
     *
     * @param category FAQ 카테고리
     * @return 필터링된 FAQ 목록
     */
    public List<Faq> getFaqsByCategory(FaqCategory category) {
        lock.readLock().lock();
        try {
            if (category == FaqCategory.ALL) {
                return new ArrayList<>(cachedFaqs);
            }
            return cachedFaqs.stream()
                    .filter(faq -> faq.getCategory() == category)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}
