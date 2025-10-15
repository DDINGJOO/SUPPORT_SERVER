package com.teambind.supportserver.report.entity.enums;

/**
 * 신고 상태
 */
public enum ReportStatus {
    PENDING,    // 대기 중
    REVIEWING,  // 검토 중
    APPROVED,   // 승인됨 (제재 적용)
    REJECTED,   // 거부됨 (신고 기각)
    WITHDRAWN   // 철회됨
}
