package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 제재 리포지토리
 */
@Repository
public interface SanctionRepository extends JpaRepository<Sanction, String> {

}
