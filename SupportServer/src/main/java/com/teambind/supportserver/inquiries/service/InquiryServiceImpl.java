package com.teambind.supportserver.inquiries.service;

import com.teambind.supportserver.common.utils.IdGenerator;
import com.teambind.supportserver.inquiries.dto.request.AnswerCreateRequest;
import com.teambind.supportserver.inquiries.dto.request.InquiryCreateRequest;
import com.teambind.supportserver.inquiries.dto.response.AnswerResponse;
import com.teambind.supportserver.inquiries.dto.response.InquiryResponse;
import com.teambind.supportserver.inquiries.entity.*;
import com.teambind.supportserver.inquiries.exceptions.ErrorCode;
import com.teambind.supportserver.inquiries.exceptions.InquiryException;
import com.teambind.supportserver.inquiries.repository.AnswerRepository;
import com.teambind.supportserver.inquiries.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 문의 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

	private final InquiryRepository inquiryRepository;
	private final AnswerRepository answerRepository;
	private final IdGenerator idGenerator;

	@Override
	@Transactional
	public InquiryResponse createInquiry(InquiryCreateRequest request) {
		log.info("Creating inquiry - title: {}, category: {}, writerId: {}",
				request.getTitle(), request.getCategory(), request.getWriterId());

		Inquiry inquiry = Inquiry.builder()
				.id(idGenerator.generateId())
				.title(request.getTitle())
				.contents(request.getContents())
				.category(request.getCategory())
				.status(InquiryStatus.UNANSWERED)
				.writerId(request.getWriterId())
				.files(new ArrayList<>()) // 이미지는 카프카로 별도 처리
				.build();

		Inquiry savedInquiry = inquiryRepository.save(inquiry);
		log.info("Inquiry created successfully - id: {}", savedInquiry.getId());

		return InquiryResponse.from(savedInquiry);
	}

	@Override
	public InquiryResponse getInquiry(String inquiryId) {
		log.debug("Getting inquiry - id: {}", inquiryId);

		Inquiry inquiry = inquiryRepository.findById(inquiryId)
				.orElseThrow(() -> new InquiryException(ErrorCode.INQUIRY_NOT_FOUND));

		return InquiryResponse.from(inquiry);
	}

	@Override
	public List<InquiryResponse> getAllInquiries() {
		log.debug("Getting all inquiries");

		return inquiryRepository.findAll().stream()
				.map(InquiryResponse::fromWithoutAnswer)
				.collect(Collectors.toList());
	}

	@Override
	public List<InquiryResponse> getInquiriesByWriter(String writerId) {
		log.debug("Getting inquiries by writer - writerId: {}", writerId);

		return inquiryRepository.findByWriterId(writerId).stream()
				.map(InquiryResponse::fromWithoutAnswer)
				.collect(Collectors.toList());
	}

	@Override
	public List<InquiryResponse> getInquiriesByCategory(InquiryCategory category) {
		log.debug("Getting inquiries by category - category: {}", category);

		return inquiryRepository.findByCategory(category).stream()
				.map(InquiryResponse::fromWithoutAnswer)
				.collect(Collectors.toList());
	}

	@Override
	public List<InquiryResponse> getInquiriesByStatus(InquiryStatus status) {
		log.debug("Getting inquiries by status - status: {}", status);

		return inquiryRepository.findByStatus(status).stream()
				.map(InquiryResponse::fromWithoutAnswer)
				.collect(Collectors.toList());
	}

	@Override
	public List<InquiryResponse> getInquiriesByWriterAndStatus(String writerId, InquiryStatus status) {
		log.debug("Getting inquiries by writer and status - writerId: {}, status: {}", writerId, status);

		return inquiryRepository.findByWriterIdAndStatus(writerId, status).stream()
				.map(InquiryResponse::fromWithoutAnswer)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public AnswerResponse createAnswer(AnswerCreateRequest request) {
		log.info("Creating answer - inquiryId: {}, writerId: {}", request.getInquiryId(), request.getWriterId());

		Inquiry inquiry = inquiryRepository.findById(request.getInquiryId())
				.orElseThrow(() -> new InquiryException(ErrorCode.INQUIRY_NOT_FOUND));

		Answer answer = Answer.builder()
				.id(idGenerator.generateId())
				.inquiry(inquiry)
				.writerId(request.getWriterId())
				.contents(request.getContents())
				.build();

		// 연관관계 편의 메소드 사용 (엔티티에서 IllegalStateException 발생 가능)
		try {
			inquiry.addAnswer(answer);
		} catch (IllegalStateException e) {
			throw new InquiryException(ErrorCode.ANSWER_ALREADY_EXISTS);
		}

		Answer savedAnswer = answerRepository.save(answer);
		log.info("Answer created successfully - id: {}", savedAnswer.getId());

		return AnswerResponse.from(savedAnswer);
	}

	@Override
	@Transactional
	public void deleteAnswer(String inquiryId) {
		log.info("Deleting answer - inquiryId: {}", inquiryId);

		Inquiry inquiry = inquiryRepository.findById(inquiryId)
				.orElseThrow(() -> new InquiryException(ErrorCode.INQUIRY_NOT_FOUND));

		// 연관관계 편의 메소드 사용 (엔티티에서 IllegalStateException 발생 가능)
		try {
			inquiry.removeAnswer();
		} catch (IllegalStateException e) {
			throw new InquiryException(ErrorCode.ANSWER_NOT_FOUND);
		}

		log.info("Answer deleted successfully - inquiryId: {}", inquiryId);
	}

	@Override
	@Transactional
	public void confirmAnswer(String inquiryId, String writerId) {
		log.info("Confirming answer - inquiryId: {}, writerId: {}", inquiryId, writerId);

		Inquiry inquiry = inquiryRepository.findById(inquiryId)
				.orElseThrow(() -> new InquiryException(ErrorCode.INQUIRY_NOT_FOUND));

		// 본인 확인
		if (!inquiry.getWriterId().equals(writerId)) {
			throw new InquiryException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		// 연관관계 편의 메소드 사용 (엔티티에서 IllegalStateException 발생 가능)
		try {
			inquiry.confirmAnswer();
		} catch (IllegalStateException e) {
			throw new InquiryException(ErrorCode.INVALID_INQUIRY_STATUS);
		}

		log.info("Answer confirmed successfully - inquiryId: {}", inquiryId);
	}

	@Override
	@Transactional
	public void deleteInquiry(String inquiryId, String writerId) {
		log.info("Deleting inquiry - id: {}, writerId: {}", inquiryId, writerId);

		Inquiry inquiry = inquiryRepository.findById(inquiryId)
				.orElseThrow(() -> new InquiryException(ErrorCode.INQUIRY_NOT_FOUND));

		// 본인 확인
		if (!inquiry.getWriterId().equals(writerId)) {
			throw new InquiryException(ErrorCode.UNAUTHORIZED_ACCESS);
		}

		inquiryRepository.delete(inquiry);
		log.info("Inquiry deleted successfully - id: {}", inquiryId);
	}
}
