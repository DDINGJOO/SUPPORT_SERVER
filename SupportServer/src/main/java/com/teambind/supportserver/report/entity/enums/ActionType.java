package com.teambind.supportserver.report.entity.enums;

/**
 * 신고 처리 액션 타입
 */
public enum ActionType {
    STATUS_CHANGED,     // 상태 변경
    ASSIGNED,           // 담당자 할당
    REVIEWED,           // 검토 완료
    SANCTION_APPLIED,   // 제재 적용
    COMMENT_ADDED       // 코멘트 추가
}
