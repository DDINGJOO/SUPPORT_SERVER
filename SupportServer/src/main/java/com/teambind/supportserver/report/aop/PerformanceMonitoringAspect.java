package com.teambind.supportserver.report.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 성능 모니터링 AOP
 *
 * <p>캐시 조회와 DB 조회의 실시간 성능을 모니터링합니다.</p>
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final ConcurrentHashMap<String, PerformanceMetrics> metricsMap = new ConcurrentHashMap<>();

    /**
     * 캐시 조회 성능 모니터링
     */
    @Around("execution(* com.teambind.supportserver.report.utils.ReportCategoryCache.get(..))")
    public Object monitorCacheAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorExecution(joinPoint, "Cache");
    }

    /**
     * DB 조회 성능 모니터링
     */
    @Around("execution(* com.teambind.supportserver.report.repository.ReportCategoryRepository.findById(..))")
    public Object monitorDbAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        return monitorExecution(joinPoint, "DB");
    }

    private Object monitorExecution(ProceedingJoinPoint joinPoint, String type) throws Throwable {
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // 나노초 -> 밀리초

            // 메트릭 기록
            PerformanceMetrics metrics = metricsMap.computeIfAbsent(type, k -> new PerformanceMetrics());
            metrics.record(duration);

            // 100번마다 로그 출력
            if (metrics.count.get() % 100 == 0) {
                log.info("[{}] Count: {}, Avg: {:.4f}ms, Min: {}ms, Max: {}ms",
                        type,
                        metrics.count.get(),
                        metrics.getAverage(),
                        metrics.minTime.get(),
                        metrics.maxTime.get());
            }
        }
    }

    /**
     * 성능 메트릭 조회
     */
    public static PerformanceMetrics getMetrics(String type) {
        return metricsMap.get(type);
    }

    /**
     * 메트릭 초기화
     */
    public static void resetMetrics() {
        metricsMap.clear();
    }

    /**
     * 성능 메트릭 데이터
     */
    public static class PerformanceMetrics {
        private final LongAdder totalTime = new LongAdder();
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(Long.MIN_VALUE);

        public void record(long duration) {
            totalTime.add(duration);
            count.incrementAndGet();

            // min 업데이트
            long currentMin = minTime.get();
            while (duration < currentMin) {
                if (minTime.compareAndSet(currentMin, duration)) {
                    break;
                }
                currentMin = minTime.get();
            }

            // max 업데이트
            long currentMax = maxTime.get();
            while (duration > currentMax) {
                if (maxTime.compareAndSet(currentMax, duration)) {
                    break;
                }
                currentMax = maxTime.get();
            }
        }

        public double getAverage() {
            long cnt = count.get();
            return cnt == 0 ? 0 : (double) totalTime.sum() / cnt;
        }

        public long getCount() {
            return count.get();
        }

        public long getMinTime() {
            return minTime.get() == Long.MAX_VALUE ? 0 : minTime.get();
        }

        public long getMaxTime() {
            return maxTime.get() == Long.MIN_VALUE ? 0 : maxTime.get();
        }

        public long getTotalTime() {
            return totalTime.sum();
        }

        @Override
        public String toString() {
            return String.format("PerformanceMetrics{count=%d, avg=%.4fms, min=%dms, max=%dms, total=%dms}",
                    getCount(), getAverage(), getMinTime(), getMaxTime(), getTotalTime());
        }
    }
}
