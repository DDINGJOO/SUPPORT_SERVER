package com.teambind.supportserver.report.controller;

import com.teambind.supportserver.report.aop.PerformanceMonitoringAspect;
import com.teambind.supportserver.report.utils.ReportCategoryCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 캐시 관리 컨트롤러
 *
 * <p>운영 중 캐시 상태를 모니터링하고 수동으로 리로드할 수 있는 엔드포인트를 제공합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>캐시 상태 조회 (헬스체크)</li>
 *   <li>캐시 수동 리로드</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
public class CacheManagementController {

    private final ReportCategoryCache reportCategoryCache;

    /**
     * 캐시 상태 조회 (헬스체크)
     *
     * 캐시 초기화 여부, 캐시된 항목 수 등의 정보를 반환합니다.
     *
     * @return 캐시 상태 정보
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", reportCategoryCache.isInitialized());
        status.put("size", reportCategoryCache.size());
        status.put("cacheType", "ReportCategory");

        log.info("Cache status requested: {}", status);

        return ResponseEntity.ok(status);
    }

    /**
     * 캐시 수동 리로드
     *
     * DB에서 모든 카테고리를 다시 조회하여 캐시를 최신 상태로 갱신합니다.
     * 운영 중 카테고리 데이터가 변경되었을 때 호출합니다.
     *
     * @return 리로드 결과 메시지
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadCache() {
        log.info("Cache reload requested");

        try {
            reportCategoryCache.reload();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache reloaded successfully");
            response.put("size", reportCategoryCache.size());

            log.info("Cache reload completed successfully. New size: {}", reportCategoryCache.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Cache reload failed: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Cache reload failed: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 성능 메트릭 조회
     *
     * 캐시 조회와 DB 조회의 실시간 성능 메트릭을 반환합니다.
     *
     * @return 성능 메트릭 정보
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        PerformanceMonitoringAspect.PerformanceMetrics cacheMetrics =
                PerformanceMonitoringAspect.getMetrics("Cache");
        PerformanceMonitoringAspect.PerformanceMetrics dbMetrics =
                PerformanceMonitoringAspect.getMetrics("DB");

        if (cacheMetrics != null) {
            Map<String, Object> cacheData = new HashMap<>();
            cacheData.put("count", cacheMetrics.getCount());
            cacheData.put("avgTimeMs", String.format("%.4f", cacheMetrics.getAverage()));
            cacheData.put("minTimeMs", cacheMetrics.getMinTime());
            cacheData.put("maxTimeMs", cacheMetrics.getMaxTime());
            cacheData.put("totalTimeMs", cacheMetrics.getTotalTime());
            metrics.put("cache", cacheData);
        }

        if (dbMetrics != null) {
            Map<String, Object> dbData = new HashMap<>();
            dbData.put("count", dbMetrics.getCount());
            dbData.put("avgTimeMs", String.format("%.4f", dbMetrics.getAverage()));
            dbData.put("minTimeMs", dbMetrics.getMinTime());
            dbData.put("maxTimeMs", dbMetrics.getMaxTime());
            dbData.put("totalTimeMs", dbMetrics.getTotalTime());
            metrics.put("db", dbData);
        }

        // 비교 정보 추가
        if (cacheMetrics != null && dbMetrics != null && cacheMetrics.getCount() > 0) {
            Map<String, Object> comparison = new HashMap<>();
            double speedup = dbMetrics.getAverage() / cacheMetrics.getAverage();
            comparison.put("speedup", String.format("%.2fx", speedup));
            comparison.put("timeSavedPerQueryMs", String.format("%.4f", dbMetrics.getAverage() - cacheMetrics.getAverage()));
            metrics.put("comparison", comparison);
        }

        return ResponseEntity.ok(metrics);
    }

    /**
     * 성능 메트릭 초기화
     *
     * 수집된 성능 메트릭을 초기화합니다.
     *
     * @return 초기화 결과
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<Map<String, Object>> resetPerformanceMetrics() {
        PerformanceMonitoringAspect.resetMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Performance metrics reset successfully");

        log.info("Performance metrics reset");

        return ResponseEntity.ok(response);
    }
}
