package com.teambind.supportserver.inquiries.repository;

import com.teambind.supportserver.inquiries.entity.Inquiry;
import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import com.teambind.supportserver.inquiries.entity.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 문의 리포지토리
 */
@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, String> {

	/**
	 * 작성자 ID로 문의 목록 조회
	 */
	List<Inquiry> findByWriterId(String writerId);

	/**
	 * 카테고리별 문의 목록 조회
	 */
	List<Inquiry> findByCategory(InquiryCategory category);

	/**
	 * 상태별 문의 목록 조회
	 */
	List<Inquiry> findByStatus(InquiryStatus status);

	/**
	 * 작성자 ID와 상태로 문의 목록 조회
	 */
	List<Inquiry> findByWriterIdAndStatus(String writerId, InquiryStatus status);
}