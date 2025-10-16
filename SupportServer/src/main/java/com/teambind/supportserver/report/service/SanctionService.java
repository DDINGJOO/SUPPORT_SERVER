package com.teambind.supportserver.report.service;

import com.teambind.supportserver.report.entity.Sanction;
import com.teambind.supportserver.report.entity.enums.SanctionType;

import java.util.List;

/**
 * 제재 서비스 인터페이스
 */
public interface SanctionService {

    /**
     * 제재 적용
     *
     * @param reportId     연관 신고 ID
     * @param targetId     제재 대상 ID
     * @param sanctionType 제재 타입
     * @param duration     제재 기간 (일 단위, 영구 정지의 경우 null)
     * @param reason       제재 사유
     * @return 적용된 제재 엔티티
     */
    Sanction createSanction(String reportId, String targetId, SanctionType sanctionType, 
                           Integer duration, String reason);

    /**
     * 자동 제재 적용 (신고 횟수 기반)
     *
     * @param reportId 연관 신고 ID
     * @param targetId 제재 대상 ID
     * @return 적용된 제재 엔티티
     */
    Sanction applyAutoSanction(String reportId, String targetId);

    /**
     * 제재 상세 조회
     *
     * @param sanctionId 제재 ID
     * @return 제재 엔티티
     */
    Sanction getSanctionById(String sanctionId);

    /**
     * 특정 대상의 활성 제재 조회
     *
     * @param targetId 제재 대상 ID
     * @return 활성 제재 목록
     */
    List<Sanction> getActiveSanctions(String targetId);

    /**
     * 특정 대상의 모든 제재 이력 조회
     *
     * @param targetId 제재 대상 ID
     * @return 제재 이력 목록
     */
    List<Sanction> getSanctionHistory(String targetId);

    /**
     * 제재 취소
     *
     * @param sanctionId 제재 ID
     * @param adminId    처리한 관리자 ID
     */
    void revokeSanction(String sanctionId, String adminId);

    /**
     * 만료된 제재 처리
     */
    void expireOldSanctions();

    /**
     * 제재 대상 여부 확인
     *
     * @param targetId 확인할 대상 ID
     * @return 활성 제재가 있으면 true
     */
    boolean isSanctioned(String targetId);
}
