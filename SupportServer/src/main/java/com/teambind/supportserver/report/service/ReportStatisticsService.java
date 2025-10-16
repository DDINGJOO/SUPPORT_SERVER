package com.teambind.supportserver.report.service;

import com.teambind.supportserver.report.entity.ReportStatistics;
import com.teambind.supportserver.report.entity.enums.ReferenceType;

import java.util.List;

/**
 * 신고 통계 서비스 인터페이스
 */
public interface ReportStatisticsService {

    /**
     * 신고 통계 조회 또는 생성
     *
     * @param referenceType  대상 타입
     * @param reportedId     신고 대상 ID
     * @param reportCategory 신고 카테고리
     * @return 신고 통계 엔티티
     */
    ReportStatistics getOrCreateStatistics(ReferenceType referenceType, String reportedId, 
                                          String reportCategory);

    /**
     * 신고 횟수 증가
     *
     * @param referenceType  대상 타입
     * @param reportedId     신고 대상 ID
     * @param reportCategory 신고 카테고리
     */
    void incrementReportCount(ReferenceType referenceType, String reportedId, String reportCategory);

    /**
     * 특정 대상의 총 신고 횟수 조회
     *
     * @param reportedId 신고 대상 ID
     * @return 총 신고 횟수
     */
    int getTotalReportCount(String reportedId);

    /**
     * 특정 대상의 카테고리별 신고 통계 조회
     *
     * @param reportedId 신고 대상 ID
     * @return 카테고리별 통계 목록
     */
    List<ReportStatistics> getStatisticsByReportedId(String reportedId);

    /**
     * 자동 제재 임계값 도달 확인
     *
     * @param reportedId     신고 대상 ID
     * @param reportCategory 신고 카테고리
     * @return 임계값 도달 여부
     */
    boolean isAutoSanctionThresholdReached(String reportedId, String reportCategory);

    /**
     * 신고 통계 초기화
     *
     * @param statId 통계 ID
     */
    void resetStatistics(String statId);
}
