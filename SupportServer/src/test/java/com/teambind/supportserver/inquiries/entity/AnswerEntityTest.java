package com.teambind.supportserver.inquiries.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Answer 엔티티 단위 테스트
 */
@DisplayName("Answer 엔티티 테스트")
class AnswerEntityTest {

	@Test
	@DisplayName("Answer 엔티티 생성 - 정상")
	void createAnswer_Success() {
		// given
		String answerId = UUID.randomUUID().toString();
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("문의 제목")
				.contents("문의 내용")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-001")
				.files(new ArrayList<>())
				.build();

		// when
		Answer answer = Answer.builder()
				.id(answerId)
				.inquiry(inquiry)
				.writerId("ADMIN-001")
				.contents("답변 내용입니다.")
				.build();

		// then
		assertThat(answer).isNotNull();
		assertThat(answer.getId()).isEqualTo(answerId);
		assertThat(answer.getInquiry()).isEqualTo(inquiry);
		assertThat(answer.getWriterId()).isEqualTo("ADMIN-001");
		assertThat(answer.getContents()).isEqualTo("답변 내용입니다.");
	}

	@Test
	@DisplayName("Answer 엔티티 생성 - Inquiry와 연관관계 설정")
	void createAnswer_WithInquiryRelationship() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("예약 관련 문의")
				.contents("예약을 취소하고 싶습니다.")
				.category(InquiryCategory.RESERVATION)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-002")
				.files(new ArrayList<>())
				.build();

		// when
		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-002")
				.contents("예약 취소는 마이페이지에서 가능합니다.")
				.build();

		// then
		assertThat(answer.getInquiry()).isNotNull();
		assertThat(answer.getInquiry().getTitle()).isEqualTo("예약 관련 문의");
		assertThat(answer.getInquiry().getWriterId()).isEqualTo("USER-002");
	}

	@Test
	@DisplayName("PrePersist 테스트 - createdAt 자동 설정")
	void prePersist_AutoSetCreatedAt() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("시간 테스트")
				.contents("PrePersist 테스트")
				.category(InquiryCategory.CHECK_IN)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-003")
				.files(new ArrayList<>())
				.build();

		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-003")
				.contents("답변 내용")
				.build();

		LocalDateTime before = LocalDateTime.now().minusSeconds(1);

		// when
		answer.onCreate();

		// then
		LocalDateTime after = LocalDateTime.now().plusSeconds(1);
		assertThat(answer.getCreatedAt()).isNotNull();
		assertThat(answer.getCreatedAt()).isBetween(before, after);
	}

	@Test
	@DisplayName("Answer 엔티티 생성 - 다양한 카테고리의 문의에 답변")
	void createAnswer_ForDifferentCategories() {
		// given & when & then
		for (InquiryCategory category : InquiryCategory.values()) {
			Inquiry inquiry = Inquiry.builder()
					.id(UUID.randomUUID().toString())
					.title("카테고리별 문의: " + category)
					.contents("문의 내용")
					.category(category)
					.status(InquiryStatus.UNANSWERED)
					.writerId("USER-004")
					.files(new ArrayList<>())
					.build();

			Answer answer = Answer.builder()
					.id(UUID.randomUUID().toString())
					.inquiry(inquiry)
					.writerId("ADMIN-004")
					.contents("카테고리 " + category + "에 대한 답변")
					.build();

			assertThat(answer.getInquiry().getCategory()).isEqualTo(category);
		}
	}

	@Test
	@DisplayName("Answer 엔티티 생성 - 긴 답변 내용")
	void createAnswer_WithLongContent() {
		// given
		String longContent = "답변 ".repeat(500); // 약 1500자
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("긴 답변 테스트")
				.contents("긴 답변이 필요한 복잡한 문의")
				.category(InquiryCategory.ETC)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-005")
				.files(new ArrayList<>())
				.build();

		// when
		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-005")
				.contents(longContent)
				.build();

		// then
		assertThat(answer.getContents()).isEqualTo(longContent);
		assertThat(answer.getContents()).hasSizeGreaterThan(1000);
	}
}
