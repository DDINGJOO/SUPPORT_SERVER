package com.teambind.supportserver.report.utils;

import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;

import java.util.Optional;

/**
 * 신고 카테고리 캐시 인터페이스
 *
 * <p>ReferenceType과 카테고리 문자열을 키로 ReportCategory를 메모리 캐시하여
 * DB 조회 없이 빠르게 카테고리 정보를 조회할 수 있도록 지원합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>복합키(ReferenceType + category) 기반 빠른 조회</li>
 *   <li>캐시 리로드를 통한 데이터 동기화</li>
 *   <li>캐시 상태 모니터링</li>
 * </ul>
 */
public interface ReportCategoryCache {

    /**
     * ReferenceType과 카테고리 문자열로 ReportCategory 조회
     *
     * @param referenceType 참조 타입
     * @param category 카테고리 문자열
     * @return ReportCategory Optional
     */
    Optional<ReportCategory> get(ReferenceType referenceType, String category);

    /**
     * ReportCategoryId로 ReportCategory 조회
     *
     * @param categoryId 카테고리 복합키
     * @return ReportCategory Optional
     */
    Optional<ReportCategory> get(ReportCategoryId categoryId);

    /**
     * 캐시가 초기화되었는지 확인
     *
     * @return 초기화 여부
     */
    boolean isInitialized();

    /**
     * 캐시된 카테고리 수 조회
     *
     * @return 캐시 사이즈
     */
    int size();

    /**
     * DB에서 모든 카테고리를 다시 로드하여 캐시 갱신
     *
     * <p>운영 중 카테고리 데이터가 변경되었을 때 수동으로 호출하여
     * 캐시를 최신 상태로 동기화할 수 있습니다.</p>
     */
    void reload();
}
