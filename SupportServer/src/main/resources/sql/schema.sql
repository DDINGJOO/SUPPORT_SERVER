-- ====================================
-- Support Server Database Schema
-- 신고 및 제재 시스템
-- ====================================
-- ====================================
-- 신고 카테고리 테이블
-- ====================================
CREATE TABLE report_categories (
    reference_type VARCHAR(20) NOT NULL COMMENT '신고 대상 타입 (PROFILE, ARTICLE, BUSINESS)',
    report_category VARCHAR(100) NOT NULL COMMENT '신고 카테고리',
    PRIMARY KEY (reference_type, report_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='신고 카테고리 마스터';

-- ====================================
-- 신고 테이블
-- ====================================
CREATE TABLE report (
    report_id VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '신고 ID',
    reporter_id VARCHAR(100) NOT NULL COMMENT '신고자 ID',
    reported_id VARCHAR(100) NOT NULL COMMENT '신고 대상 ID',
    reference_type VARCHAR(20) NOT NULL COMMENT '신고 대상 타입',
    report_category VARCHAR(100) NOT NULL COMMENT '신고 카테고리',
    reason VARCHAR(100) NOT NULL COMMENT '신고 사유 (신고자 작성)',
    reported_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '신고 일시',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '신고 상태',

    CONSTRAINT fk_report_category
        FOREIGN KEY (reference_type, report_category)
        REFERENCES report_categories(reference_type, report_category),

    CONSTRAINT uk_report_per_user
        UNIQUE KEY (reporter_id, reference_type, reported_id),

    INDEX idx_report_reporter_id (reporter_id),
    INDEX idx_report_reported_id (reported_id),
    INDEX idx_report_reported_at (reported_at),
    INDEX idx_report_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='신고 내역';

-- ====================================
-- 제재 규칙 테이블
-- ====================================
CREATE TABLE sanction_rules (
    rule_id VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '규칙 ID',
    reference_type VARCHAR(20) NOT NULL COMMENT '대상 타입',
    report_threshold INT NOT NULL COMMENT '신고 임계값 (N회 이상 시)',
    sanction_type VARCHAR(20) NOT NULL COMMENT '적용할 제재 타입',
    duration INT NULL COMMENT '제재 기간 (일 단위, NULL이면 영구)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '규칙 활성화 여부',

    INDEX idx_sanction_rules_reference_type (reference_type),
    INDEX idx_sanction_rules_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='자동 제재 규칙';

-- ====================================
-- 제재 테이블
-- ====================================
CREATE TABLE sanctions (
    sanction_id VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '제재 ID',
    report_id VARCHAR(100) NOT NULL COMMENT '연관 신고 ID',
    target_id VARCHAR(100) NOT NULL COMMENT '제재 대상 ID',
    sanction_type VARCHAR(20) NOT NULL COMMENT '제재 타입',
    duration INT NULL COMMENT '제재 기간 (일 단위)',
    reason VARCHAR(500) NOT NULL COMMENT '제재 사유',
    sanctioned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '제재 시작 일시',
    expires_at DATETIME(6) NULL COMMENT '제재 만료 일시',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '제재 상태',

    CONSTRAINT fk_sanctions_report
        FOREIGN KEY (report_id)
        REFERENCES report(report_id),

    INDEX idx_sanctions_target_id (target_id),
    INDEX idx_sanctions_status (status),
    INDEX idx_sanctions_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='제재 내역';

-- ====================================
-- 신고 처리 이력 테이블
-- ====================================
CREATE TABLE report_history (
    history_id VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '이력 ID',
    report_id VARCHAR(100) NOT NULL COMMENT '신고 ID',
    admin_id VARCHAR(100) NULL COMMENT '처리한 관리자 ID',
    previous_status VARCHAR(20) NULL COMMENT '이전 상태',
    new_status VARCHAR(20) NOT NULL COMMENT '새 상태',
    action_type VARCHAR(30) NOT NULL COMMENT '액션 타입',
    comment TEXT NULL COMMENT '처리 의견',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',

    CONSTRAINT fk_report_history_report
        FOREIGN KEY (report_id)
        REFERENCES report(report_id),

    INDEX idx_report_history_report_id (report_id),
    INDEX idx_report_history_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='신고 처리 이력';

-- ====================================
-- 신고 통계 집계 테이블 (추후 추가 예정)
-- ====================================
CREATE TABLE report_statistics (
    stat_id VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '통계 ID',
    reference_type VARCHAR(20) NOT NULL COMMENT '대상 타입',
    reported_id VARCHAR(100) NOT NULL COMMENT '신고 대상 ID',
    report_category VARCHAR(100) NOT NULL COMMENT '신고 카테고리',
    report_count INT NOT NULL DEFAULT 0 COMMENT '신고 횟수',
    last_reported_at DATETIME(6) NOT NULL COMMENT '최근 신고 일시',

    CONSTRAINT fk_report_statistics_category
        FOREIGN KEY (reference_type, report_category)
        REFERENCES report_categories(reference_type, report_category),

    CONSTRAINT uk_report_statistics
        UNIQUE KEY (reference_type, reported_id, report_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='신고 통계 집계 (성능 최적화용)';

-- ====================================
-- 초기 데이터: 신고 카테고리
-- ====================================

-- 프로필 신고 카테고리
INSERT INTO report_categories (reference_type, report_category) VALUES
('PROFILE', '욕설, 비속어, 음란'),
('PROFILE', '부적절한 닉네임'),
('PROFILE', '부적절한 프로필 사진'),
('PROFILE', '부적절한 자기소개'),
('PROFILE', '기타');

-- 게시글 신고 카테고리
INSERT INTO report_categories (reference_type, report_category) VALUES
('ARTICLE', '욕설, 비속어, 음란성 내용을 포함한 게시글'),
('ARTICLE', '도배, 스팸, 광고성 게시글'),
('ARTICLE', '분란을 조장하는 게시글'),
('ARTICLE', '타업체를 광고한 게시글'),
('ARTICLE', '허위 사기성 내용'),
('ARTICLE', '기타');
