package com.teambind.supportserver.inquiries.service;

import com.teambind.supportserver.inquiries.dto.request.AnswerCreateRequest;
import com.teambind.supportserver.inquiries.dto.request.InquiryCreateRequest;
import com.teambind.supportserver.inquiries.dto.response.AnswerResponse;
import com.teambind.supportserver.inquiries.dto.response.InquiryResponse;
import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import com.teambind.supportserver.inquiries.entity.InquiryStatus;

import java.util.List;

/**
 * 문의 서비스 인터페이스
 */
public interface InquiryService {

	/**
	 * 문의 생성
	 *
	 * @param request 문의 생성 요청 DTO
	 * @return 생성된 문의 응답 DTO
	 */
	InquiryResponse createInquiry(InquiryCreateRequest request);

	/**
	 * 문의 상세 조회
	 *
	 * @param inquiryId 문의 ID
	 * @return 문의 응답 DTO (답변 정보 포함)
	 */
	InquiryResponse getInquiry(String inquiryId);

	/**
	 * 문의 목록 조회 (전체)
	 *
	 * @return 문의 목록
	 */
	List<InquiryResponse> getAllInquiries();

	/**
	 * 작성자별 문의 목록 조회
	 *
	 * @param writerId 작성자 ID
	 * @return 문의 목록
	 */
	List<InquiryResponse> getInquiriesByWriter(String writerId);

	/**
	 * 카테고리별 문의 목록 조회
	 *
	 * @param category 문의 카테고리
	 * @return 문의 목록
	 */
	List<InquiryResponse> getInquiriesByCategory(InquiryCategory category);

	/**
	 * 상태별 문의 목록 조회
	 *
	 * @param status 문의 상태
	 * @return 문의 목록
	 */
	List<InquiryResponse> getInquiriesByStatus(InquiryStatus status);

	/**
	 * 작성자 및 상태별 문의 목록 조회
	 *
	 * @param writerId 작성자 ID
	 * @param status   문의 상태
	 * @return 문의 목록
	 */
	List<InquiryResponse> getInquiriesByWriterAndStatus(String writerId, InquiryStatus status);

	/**
	 * 답변 생성 (문의에 답변 등록)
	 *
	 * @param request 답변 생성 요청 DTO
	 * @return 생성된 답변 응답 DTO
	 */
	AnswerResponse createAnswer(AnswerCreateRequest request);

	/**
	 * 답변 삭제
	 *
	 * @param inquiryId 문의 ID
	 */
	void deleteAnswer(String inquiryId);

	/**
	 * 답변 확인 (사용자가 답변을 확인했을 때)
	 *
	 * @param inquiryId 문의 ID
	 * @param writerId  작성자 ID (본인 확인용)
	 */
	void confirmAnswer(String inquiryId, String writerId);

	/**
	 * 문의 삭제
	 *
	 * @param inquiryId 문의 ID
	 * @param writerId  작성자 ID (본인 확인용)
	 */
	void deleteInquiry(String inquiryId, String writerId);
}
