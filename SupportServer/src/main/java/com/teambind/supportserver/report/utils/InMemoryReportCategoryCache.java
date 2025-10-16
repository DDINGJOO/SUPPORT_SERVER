package com.teambind.supportserver.report.utils;

import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.repository.ReportCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 기반 ReportCategory 캐시 구현체
 *
 * <p>애플리케이션 시작 시 DB에서 모든 카테고리를 로드하여 ConcurrentHashMap에 캐싱합니다.
 * 스레드 세이프하며, 운영 중 수동 리로드를 통해 DB와 동기화할 수 있습니다.</p>
 *
 * <p>주요 특징:</p>
 * <ul>
 *   <li>ApplicationRunner를 통한 자동 초기화</li>
 *   <li>ConcurrentHashMap 기반 스레드 세이프 캐시</li>
 *   <li>단일 쿼리로 전체 카테고리 로드 (N+1 방지)</li>
 *   <li>초기 로딩 실패 시 재시도 전략</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryReportCategoryCache implements ReportCategoryCache, ApplicationRunner {

    private final ReportCategoryRepository reportCategoryRepository;

    /**
     * 복합키(ReferenceType + category)를 키로 하는 캐시 맵
     */
    private final Map<ReportCategoryId, ReportCategory> cache = new ConcurrentHashMap<>();

    /**
     * 캐시 초기화 완료 플래그
     */
    private volatile boolean initialized = false;

    /**
     * 최대 재시도 횟수
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 재시도 간 대기 시간(밀리초)
     */
    private static final long RETRY_DELAY_MS = 2000;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting ReportCategory cache initialization...");
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRY_COUNT && !success) {
            attempt++;
            try {
                loadCache();
                success = true;
                log.info("ReportCategory cache initialized successfully. Total categories: {}", cache.size());
            } catch (Exception e) {
                log.error("Failed to initialize ReportCategory cache (attempt {}/{}): {}",
                        attempt, MAX_RETRY_COUNT, e.getMessage(), e);

                if (attempt < MAX_RETRY_COUNT) {
                    try {
                        log.info("Retrying in {} ms...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Cache initialization retry interrupted", ie);
                        break;
                    }
                } else {
                    log.error("ReportCategory cache initialization failed after {} attempts. Application will continue but category lookups may fail.", MAX_RETRY_COUNT);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void reload() {
        log.info("Reloading ReportCategory cache...");
        try {
            loadCache();
            log.info("ReportCategory cache reloaded successfully. Total categories: {}", cache.size());
        } catch (Exception e) {
            log.error("Failed to reload ReportCategory cache: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reload category cache", e);
        }
    }

    @Override
    public Optional<ReportCategory> get(ReferenceType referenceType, String category) {
        if (!initialized) {
            log.warn("Cache is not initialized yet. Attempting to access category: {}:{}", referenceType, category);
            return Optional.empty();
        }

        // 카테고리 문자열 정규화 (trim 및 소문자)
        String normalizedCategory = normalizeCategory(category);
        ReportCategoryId categoryId = new ReportCategoryId(referenceType, normalizedCategory);

        return Optional.ofNullable(cache.get(categoryId));
    }

    @Override
    public Optional<ReportCategory> get(ReportCategoryId categoryId) {
        if (!initialized) {
            log.warn("Cache is not initialized yet. Attempting to access categoryId: {}", categoryId);
            return Optional.empty();
        }

        // 복합키의 카테고리 문자열 정규화
        String normalizedCategory = normalizeCategory(categoryId.getReportCategory());
        ReportCategoryId normalizedId = new ReportCategoryId(
                categoryId.getReferenceType(),
                normalizedCategory
        );

        return Optional.ofNullable(cache.get(normalizedId));
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public int size() {
        return cache.size();
    }

    /**
     * DB에서 모든 카테고리를 조회하여 캐시에 로드
     *
     * <p>단일 쿼리로 모든 데이터를 가져와 N+1 문제를 방지합니다.
     * 기존 캐시를 clear하고 새로운 데이터로 교체합니다.</p>
     */
    @Transactional(readOnly = true)
    protected void loadCache() {
        List<ReportCategory> categories = reportCategoryRepository.findAll();

        // 새로운 맵을 생성하여 원자적으로 교체
        Map<ReportCategoryId, ReportCategory> newCache = categories.stream()
                .collect(Collectors.toMap(
                        category -> {
                            // 카테고리 문자열 정규화하여 키 생성
                            String normalizedCategory = normalizeCategory(category.getId().getReportCategory());
                            return new ReportCategoryId(
                                    category.getId().getReferenceType(),
                                    normalizedCategory
                            );
                        },
                        category -> category,
                        (existing, replacement) -> {
                            log.warn("Duplicate category found: {}. Keeping first occurrence.", existing.getId());
                            return existing;
                        }
                ));

        // 기존 캐시를 비우고 새 데이터로 교체
        cache.clear();
        cache.putAll(newCache);

        // 초기화 완료 플래그 설정
        initialized = true;

        log.debug("Loaded {} categories into cache", cache.size());
    }

    /**
     * 카테고리 문자열 정규화
     *
     * <p>일관성을 위해 trim 및 소문자 변환을 수행합니다.</p>
     *
     * @param category 원본 카테고리 문자열
     * @return 정규화된 카테고리 문자열
     */
    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        return category.trim().toLowerCase();
    }
}
