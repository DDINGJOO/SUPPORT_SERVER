package com.teambind.supportserver.inquiries.controller;

import com.teambind.supportserver.inquiries.dto.request.AnswerCreateRequest;
import com.teambind.supportserver.inquiries.dto.request.InquiryCreateRequest;
import com.teambind.supportserver.inquiries.dto.response.AnswerResponse;
import com.teambind.supportserver.inquiries.dto.response.InquiryResponse;
import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import com.teambind.supportserver.inquiries.entity.InquiryStatus;
import com.teambind.supportserver.inquiries.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 문의 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

	private final InquiryService inquiryService;

	/**
	 * 문의 생성
	 *
	 * @param request 문의 생성 요청 DTO
	 * @return 생성된 문의 응답 DTO
	 */
	@PostMapping
	public ResponseEntity<InquiryResponse> createInquiry(@Valid @RequestBody InquiryCreateRequest request) {
		log.info("POST /api/v1/inquiries - Creating inquiry: title={}, category={}, writerId={}",
				request.getTitle(), request.getCategory(), request.getWriterId());

		InquiryResponse response = inquiryService.createInquiry(request);

		log.info("Inquiry created successfully - id={}", response.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 문의 상세 조회
	 *
	 * @param inquiryId 문의 ID
	 * @return 문의 응답 DTO (답변 정보 포함)
	 */
	@GetMapping("/{inquiryId}")
	public ResponseEntity<InquiryResponse> getInquiry(@PathVariable String inquiryId) {
		log.info("GET /api/v1/inquiries/{} - Fetching inquiry", inquiryId);

		InquiryResponse response = inquiryService.getInquiry(inquiryId);

		return ResponseEntity.ok(response);
	}

	/**
	 * 문의 목록 조회 (필터링 가능)
	 *
	 * @param writerId 작성자 ID (선택)
	 * @param category 카테고리 (선택)
	 * @param status   상태 (선택)
	 * @return 문의 목록
	 */
	@GetMapping
	public ResponseEntity<List<InquiryResponse>> getInquiries(
			@RequestParam(required = false) String writerId,
			@RequestParam(required = false) InquiryCategory category,
			@RequestParam(required = false) InquiryStatus status) {
		log.info("GET /api/v1/inquiries - Fetching inquiries: writerId={}, category={}, status={}",
				writerId, category, status);

		List<InquiryResponse> responses;

		// 필터 조합에 따른 조회
		if (writerId != null && status != null) {
			responses = inquiryService.getInquiriesByWriterAndStatus(writerId, status);
		} else if (writerId != null) {
			responses = inquiryService.getInquiriesByWriter(writerId);
		} else if (category != null) {
			responses = inquiryService.getInquiriesByCategory(category);
		} else if (status != null) {
			responses = inquiryService.getInquiriesByStatus(status);
		} else {
			responses = inquiryService.getAllInquiries();
		}

		log.info("Returned {} inquiries", responses.size());
		return ResponseEntity.ok(responses);
	}

	/**
	 * 문의 삭제
	 *
	 * @param inquiryId 문의 ID
	 * @param writerId  작성자 ID (본인 확인용)
	 * @return 삭제 결과
	 */
	@DeleteMapping("/{inquiryId}")
	public ResponseEntity<Void> deleteInquiry(
			@PathVariable String inquiryId,
			@RequestParam String writerId) {
		log.info("DELETE /api/v1/inquiries/{} - Deleting inquiry: writerId={}", inquiryId, writerId);

		inquiryService.deleteInquiry(inquiryId, writerId);

		log.info("Inquiry deleted successfully - id={}", inquiryId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 답변 생성
	 *
	 * @param request 답변 생성 요청 DTO
	 * @return 생성된 답변 응답 DTO
	 */
	@PostMapping("/answers")
	public ResponseEntity<AnswerResponse> createAnswer(@Valid @RequestBody AnswerCreateRequest request) {
		log.info("POST /api/v1/inquiries/answers - Creating answer: inquiryId={}, writerId={}",
				request.getInquiryId(), request.getWriterId());

		AnswerResponse response = inquiryService.createAnswer(request);

		log.info("Answer created successfully - id={}", response.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 답변 삭제
	 *
	 * @param inquiryId 문의 ID
	 * @return 삭제 결과
	 */
	@DeleteMapping("/{inquiryId}/answer")
	public ResponseEntity<Void> deleteAnswer(@PathVariable String inquiryId) {
		log.info("DELETE /api/v1/inquiries/{}/answer - Deleting answer", inquiryId);

		inquiryService.deleteAnswer(inquiryId);

		log.info("Answer deleted successfully - inquiryId={}", inquiryId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 답변 확인 (사용자가 답변을 확인했을 때)
	 *
	 * @param inquiryId 문의 ID
	 * @param writerId  작성자 ID (본인 확인용)
	 * @return 확인 결과
	 */
	@PatchMapping("/{inquiryId}/confirm")
	public ResponseEntity<Void> confirmAnswer(
			@PathVariable String inquiryId,
			@RequestParam String writerId) {
		log.info("PATCH /api/v1/inquiries/{}/confirm - Confirming answer: writerId={}", inquiryId, writerId);

		inquiryService.confirmAnswer(inquiryId, writerId);

		log.info("Answer confirmed successfully - inquiryId={}", inquiryId);
		return ResponseEntity.ok().build();
	}
}
