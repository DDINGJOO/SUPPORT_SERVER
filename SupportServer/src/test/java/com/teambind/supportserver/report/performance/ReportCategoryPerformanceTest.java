package com.teambind.supportserver.report.performance;

import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.repository.ReportCategoryRepository;
import com.teambind.supportserver.report.utils.ReportCategoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ReportCategory 조회 성능 비교 테스트
 *
 * <p>캐시 방식 vs DB 조회 방식의 성능을 비교합니다.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
public class ReportCategoryPerformanceTest {

    @Autowired
    private ReportCategoryCache reportCategoryCache;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    private List<ReportCategoryId> testCategoryIds;
    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random();

        // 테스트용 카테고리 ID 목록 생성
        testCategoryIds = new ArrayList<>();
        testCategoryIds.add(new ReportCategoryId(ReferenceType.PROFILE, "spam"));
        testCategoryIds.add(new ReportCategoryId(ReferenceType.PROFILE, "harassment"));
        testCategoryIds.add(new ReportCategoryId(ReferenceType.ARTICLE, "inappropriate"));
        testCategoryIds.add(new ReportCategoryId(ReferenceType.BUSINESS, "hate_speech"));
        // 더 많은 카테고리 추가 가능
    }

    @Test
    void compareCacheVsDbPerformance() {
        int iterations = 10000; // 조회 횟수

        System.out.println("=== Performance Test: Cache vs DB ===");
        System.out.println("Iterations: " + iterations);
        System.out.println();

        // 1. 캐시 방식 성능 측정
        long cacheTime = measureCachePerformance(iterations);

        // 2. DB 조회 방식 성능 측정
        long dbTime = measureDbPerformance(iterations);

        // 3. 결과 출력
        System.out.println("\n=== Results ===");
        System.out.println("Cache Method: " + cacheTime + "ms");
        System.out.println("DB Method: " + dbTime + "ms");
        System.out.println("Speedup: " + String.format("%.2f", (double) dbTime / cacheTime) + "x");
        System.out.println("Time Saved: " + (dbTime - cacheTime) + "ms");
    }

    private long measureCachePerformance(int iterations) {
        StopWatch stopWatch = new StopWatch("Cache Performance");
        stopWatch.start();

        for (int i = 0; i < iterations; i++) {
            ReportCategoryId categoryId = getRandomCategoryId();
            reportCategoryCache.get(categoryId);
        }

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();

        System.out.println("Cache Method:");
        System.out.println("  Total Time: " + totalTime + "ms");
        System.out.println("  Avg Time: " + String.format("%.4f", (double) totalTime / iterations) + "ms per query");

        return totalTime;
    }

    private long measureDbPerformance(int iterations) {
        StopWatch stopWatch = new StopWatch("DB Performance");
        stopWatch.start();

        for (int i = 0; i < iterations; i++) {
            ReportCategoryId categoryId = getRandomCategoryId();
            reportCategoryRepository.findById(categoryId);
        }

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();

        System.out.println("\nDB Method:");
        System.out.println("  Total Time: " + totalTime + "ms");
        System.out.println("  Avg Time: " + String.format("%.4f", (double) totalTime / iterations) + "ms per query");

        return totalTime;
    }

    /**
     * 동시성 테스트 (멀티스레드 환경)
     */
    @Test
    void compareConcurrentPerformance() throws InterruptedException {
        int threads = 10;
        int iterationsPerThread = 1000;

        System.out.println("=== Concurrent Performance Test ===");
        System.out.println("Threads: " + threads);
        System.out.println("Iterations per thread: " + iterationsPerThread);
        System.out.println();

        // 1. 캐시 방식 동시성 테스트
        long cacheTime = measureConcurrentCache(threads, iterationsPerThread);

        // 2. DB 방식 동시성 테스트
        long dbTime = measureConcurrentDb(threads, iterationsPerThread);

        // 3. 결과 출력
        System.out.println("\n=== Concurrent Results ===");
        System.out.println("Cache Method: " + cacheTime + "ms");
        System.out.println("DB Method: " + dbTime + "ms");
        System.out.println("Speedup: " + String.format("%.2f", (double) dbTime / cacheTime) + "x");
    }

    private long measureConcurrentCache(int threadCount, int iterations) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        StopWatch stopWatch = new StopWatch("Concurrent Cache");
        stopWatch.start();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    ReportCategoryId categoryId = getRandomCategoryId();
                    reportCategoryCache.get(categoryId);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();

        System.out.println("Concurrent Cache Method:");
        System.out.println("  Total Time: " + totalTime + "ms");
        System.out.println("  Throughput: " + String.format("%.2f", (double) (threadCount * iterations) / totalTime * 1000) + " queries/sec");

        return totalTime;
    }

    private long measureConcurrentDb(int threadCount, int iterations) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        StopWatch stopWatch = new StopWatch("Concurrent DB");
        stopWatch.start();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    ReportCategoryId categoryId = getRandomCategoryId();
                    reportCategoryRepository.findById(categoryId);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();
        long totalTime = stopWatch.getTotalTimeMillis();

        System.out.println("\nConcurrent DB Method:");
        System.out.println("  Total Time: " + totalTime + "ms");
        System.out.println("  Throughput: " + String.format("%.2f", (double) (threadCount * iterations) / totalTime * 1000) + " queries/sec");

        return totalTime;
    }

    private ReportCategoryId getRandomCategoryId() {
        return testCategoryIds.get(random.nextInt(testCategoryIds.size()));
    }
}
