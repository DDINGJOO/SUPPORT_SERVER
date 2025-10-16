package com.teambind.supportserver.faq.repository;

import com.teambind.supportserver.faq.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * FAQ 리포지토리
 */
@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

}
