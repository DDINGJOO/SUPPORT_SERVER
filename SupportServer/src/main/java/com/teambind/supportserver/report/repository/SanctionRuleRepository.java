package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.SanctionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 제재 규칙 리포지토리
 */
@Repository
public interface SanctionRuleRepository extends JpaRepository<SanctionRule, String> {

}
