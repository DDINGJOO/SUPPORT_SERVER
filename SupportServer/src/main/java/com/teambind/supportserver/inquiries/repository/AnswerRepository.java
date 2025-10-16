package com.teambind.supportserver.inquiries.repository;

import com.teambind.supportserver.inquiries.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 답변 리포지토리
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {

	/**
	 * 문의 ID로 답변 조회
	 */
	Optional<Answer> findByInquiryId(String inquiryId);

	/**
	 * 작성자 ID로 답변 존재 여부 확인
	 */
	boolean existsByWriterId(String writerId);
}
