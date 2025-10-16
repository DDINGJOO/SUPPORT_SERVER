package com.teambind.supportserver.inquiries.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Inquiry 엔티티 단위 테스트
 */
@DisplayName("Inquiry 엔티티 테스트")
class InquiryEntityTest {

	@Test
	@DisplayName("Inquiry 엔티티 생성 - 정상")
	void createInquiry_Success() {
		// given
		String inquiryId = UUID.randomUUID().toString();
		List<InquiryFile> files = new ArrayList<>();
		files.add(InquiryFile.builder()
				.imageId("IMG-001")
				.imageUrl("https://example.com/image1.jpg")
				.fileName("test1.jpg")
				.build());

		// when
		Inquiry inquiry = Inquiry.builder()
				.id(inquiryId)
				.title("문의 제목")
				.contents("문의 내용입니다.")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-001")
				.files(files)
				.build();

		// then
		assertThat(inquiry).isNotNull();
		assertThat(inquiry.getId()).isEqualTo(inquiryId);
		assertThat(inquiry.getTitle()).isEqualTo("문의 제목");
		assertThat(inquiry.getContents()).isEqualTo("문의 내용입니다.");
		assertThat(inquiry.getCategory()).isEqualTo(InquiryCategory.PAYMENT);
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.UNANSWERED);
		assertThat(inquiry.getWriterId()).isEqualTo("USER-001");
		assertThat(inquiry.getFiles()).hasSize(1);
	}

	@Test
	@DisplayName("Inquiry 엔티티 생성 - 파일 없이 생성 가능")
	void createInquiry_WithoutFiles() {
		// given
		String inquiryId = UUID.randomUUID().toString();

		// when
		Inquiry inquiry = Inquiry.builder()
				.id(inquiryId)
				.title("파일 없는 문의")
				.contents("파일을 첨부하지 않았습니다.")
				.category(InquiryCategory.CHECK_IN)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-002")
				.files(new ArrayList<>())
				.build();

		// then
		assertThat(inquiry).isNotNull();
		assertThat(inquiry.getFiles()).isEmpty();
	}

	@Test
	@DisplayName("파일 검증 - 5개까지 첨부 가능")
	void validateFiles_FiveFiles_Success() {
		// given
		String inquiryId = UUID.randomUUID().toString();
		List<InquiryFile> files = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			files.add(InquiryFile.builder()
					.imageId("IMG-" + i)
					.imageUrl("https://example.com/image" + i + ".jpg")
					.fileName("test" + i + ".jpg")
					.build());
		}

		// when
		Inquiry inquiry = Inquiry.builder()
				.id(inquiryId)
				.title("파일 5개 첨부")
				.contents("파일 5개를 첨부했습니다.")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-003")
				.files(files)
				.build();

		// then
		assertThat(inquiry.getFiles()).hasSize(5);
	}

	@Test
	@DisplayName("파일 검증 - 6개 이상 첨부 시 예외 발생 (PrePersist)")
	void validateFiles_MoreThanFive_ThrowsException() {
		// given
		String inquiryId = UUID.randomUUID().toString();
		List<InquiryFile> files = new ArrayList<>();
		for (int i = 1; i <= 6; i++) {
			files.add(InquiryFile.builder()
					.imageId("IMG-" + i)
					.imageUrl("https://example.com/image" + i + ".jpg")
					.fileName("test" + i + ".jpg")
					.build());
		}

		Inquiry inquiry = Inquiry.builder()
				.id(inquiryId)
				.title("파일 6개 첨부")
				.contents("파일 6개를 첨부했습니다.")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-003")
				.files(files)
				.build();

		// when & then
		assertThatThrownBy(() -> inquiry.onCreate())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("파일은 최대 5개까지 첨부할 수 있습니다.");
	}

	@Test
	@DisplayName("카테고리별 Inquiry 생성 - 모든 카테고리 테스트")
	void createInquiry_AllCategories() {
		// given & when & then
		for (InquiryCategory category : InquiryCategory.values()) {
			Inquiry inquiry = Inquiry.builder()
					.id(UUID.randomUUID().toString())
					.title("카테고리 테스트")
					.contents("카테고리: " + category)
					.category(category)
					.status(InquiryStatus.UNANSWERED)
					.writerId("USER-004")
					.files(new ArrayList<>())
					.build();

			assertThat(inquiry.getCategory()).isEqualTo(category);
		}
	}

	@Test
	@DisplayName("상태별 Inquiry 생성 - 모든 상태 테스트")
	void createInquiry_AllStatuses() {
		// given & when & then
		for (InquiryStatus status : InquiryStatus.values()) {
			Inquiry inquiry = Inquiry.builder()
					.id(UUID.randomUUID().toString())
					.title("상태 테스트")
					.contents("상태: " + status)
					.category(InquiryCategory.ETC)
					.status(status)
					.writerId("USER-005")
					.files(new ArrayList<>())
					.build();

			assertThat(inquiry.getStatus()).isEqualTo(status);
		}
	}

	@Test
	@DisplayName("PrePersist 테스트 - createdAt과 updatedAt 자동 설정")
	void prePersist_AutoSetTimestamps() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("시간 테스트")
				.contents("PrePersist 테스트")
				.category(InquiryCategory.RESERVATION)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-006")
				.files(new ArrayList<>())
				.build();

		LocalDateTime before = LocalDateTime.now().minusSeconds(1);

		// when
		inquiry.onCreate();

		// then
		LocalDateTime after = LocalDateTime.now().plusSeconds(1);
		assertThat(inquiry.getCreatedAt()).isNotNull();
		assertThat(inquiry.getUpdatedAt()).isNotNull();
		assertThat(inquiry.getCreatedAt()).isBetween(before, after);
		assertThat(inquiry.getUpdatedAt()).isBetween(before, after);
		assertThat(inquiry.getCreatedAt()).isEqualTo(inquiry.getUpdatedAt());
	}

	@Test
	@DisplayName("PreUpdate 테스트 - updatedAt 자동 갱신")
	void preUpdate_AutoUpdateTimestamp() throws InterruptedException {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("업데이트 테스트")
				.contents("PreUpdate 테스트")
				.category(InquiryCategory.REVIEW_REPORT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-007")
				.files(new ArrayList<>())
				.build();

		inquiry.onCreate();
		LocalDateTime createdAt = inquiry.getCreatedAt();
		LocalDateTime initialUpdatedAt = inquiry.getUpdatedAt();

		Thread.sleep(100); // 시간 차이를 위해 대기

		// when
		inquiry.onUpdate();

		// then
		assertThat(inquiry.getCreatedAt()).isEqualTo(createdAt);
		assertThat(inquiry.getUpdatedAt()).isAfter(initialUpdatedAt);
	}

	@Test
	@DisplayName("답변 등록 - 정상")
	void addAnswer_Success() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("결제 문의")
				.contents("결제가 안 됩니다.")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-001")
				.files(new ArrayList<>())
				.build();

		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-001")
				.contents("결제는 마이페이지에서 가능합니다.")
				.build();

		LocalDateTime before = LocalDateTime.now().minusSeconds(1);

		// when
		inquiry.addAnswer(answer);

		// then
		LocalDateTime after = LocalDateTime.now().plusSeconds(1);
		assertThat(inquiry.getAnswer()).isEqualTo(answer);
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
		assertThat(inquiry.getAnsweredAt()).isNotNull();
		assertThat(inquiry.getAnsweredAt()).isBetween(before, after);
		assertThat(inquiry.hasAnswer()).isTrue();
	}

	@Test
	@DisplayName("답변 등록 - 이미 답변이 있으면 예외 발생")
	void addAnswer_AlreadyAnswered_ThrowsException() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("예약 문의")
				.contents("예약을 취소하고 싶습니다.")
				.category(InquiryCategory.RESERVATION)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-002")
				.files(new ArrayList<>())
				.build();

		Answer firstAnswer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-001")
				.contents("첫 번째 답변")
				.build();

		Answer secondAnswer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-002")
				.contents("두 번째 답변")
				.build();

		inquiry.addAnswer(firstAnswer);

		// when & then
		assertThatThrownBy(() -> inquiry.addAnswer(secondAnswer))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("이미 답변이 등록된 문의입니다.");
	}

	@Test
	@DisplayName("답변 삭제 - 정상")
	void removeAnswer_Success() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("입실 문의")
				.contents("입실 시간을 변경하고 싶습니다.")
				.category(InquiryCategory.CHECK_IN)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-003")
				.files(new ArrayList<>())
				.build();

		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-003")
				.contents("입실 시간 변경은 1일 전까지 가능합니다.")
				.build();

		inquiry.addAnswer(answer);

		// when
		inquiry.removeAnswer();

		// then
		assertThat(inquiry.getAnswer()).isNull();
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.UNANSWERED);
		assertThat(inquiry.getAnsweredAt()).isNull();
		assertThat(inquiry.hasAnswer()).isFalse();
	}

	@Test
	@DisplayName("답변 삭제 - 답변이 없으면 예외 발생")
	void removeAnswer_NoAnswer_ThrowsException() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("리뷰 신고 문의")
				.contents("부적절한 리뷰를 신고하고 싶습니다.")
				.category(InquiryCategory.REVIEW_REPORT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-004")
				.files(new ArrayList<>())
				.build();

		// when & then
		assertThatThrownBy(() -> inquiry.removeAnswer())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("등록된 답변이 없습니다.");
	}

	@Test
	@DisplayName("답변 확인 - 정상")
	void confirmAnswer_Success() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("기타 문의")
				.contents("문의 내용")
				.category(InquiryCategory.ETC)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-005")
				.files(new ArrayList<>())
				.build();

		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-005")
				.contents("답변 내용")
				.build();

		inquiry.addAnswer(answer);

		// when
		inquiry.confirmAnswer();

		// then
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.CONFIRMED);
	}

	@Test
	@DisplayName("답변 확인 - ANSWERED 상태가 아니면 예외 발생")
	void confirmAnswer_NotAnswered_ThrowsException() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("결제 문의")
				.contents("문의 내용")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-006")
				.files(new ArrayList<>())
				.build();

		// when & then
		assertThatThrownBy(() -> inquiry.confirmAnswer())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("답변이 완료된 문의만 확인할 수 있습니다.");
	}

	@Test
	@DisplayName("답변 존재 여부 확인 - 답변이 있는 경우")
	void hasAnswer_WithAnswer_ReturnsTrue() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("예약 문의")
				.contents("예약을 변경하고 싶습니다.")
				.category(InquiryCategory.RESERVATION)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-007")
				.files(new ArrayList<>())
				.build();

		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-007")
				.contents("예약 변경은 고객센터로 연락주세요.")
				.build();

		inquiry.addAnswer(answer);

		// when & then
		assertThat(inquiry.hasAnswer()).isTrue();
	}

	@Test
	@DisplayName("답변 존재 여부 확인 - 답변이 없는 경우")
	void hasAnswer_WithoutAnswer_ReturnsFalse() {
		// given
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("입실 문의")
				.contents("입실 시간을 확인하고 싶습니다.")
				.category(InquiryCategory.CHECK_IN)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-008")
				.files(new ArrayList<>())
				.build();

		// when & then
		assertThat(inquiry.hasAnswer()).isFalse();
	}

	@Test
	@DisplayName("연관관계 편의 메소드 통합 시나리오 - 답변 등록부터 확인까지")
	void relationshipConvenienceMethod_IntegrationScenario() {
		// given - 문의 생성
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("결제 오류 문의")
				.contents("결제가 두 번 처리되었습니다.")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-009")
				.files(new ArrayList<>())
				.build();

		assertThat(inquiry.hasAnswer()).isFalse();
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.UNANSWERED);

		// when - 답변 등록
		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-009")
				.contents("중복 결제는 자동으로 취소됩니다.")
				.build();

		inquiry.addAnswer(answer);

		// then - 답변 등록 후 상태 확인
		assertThat(inquiry.hasAnswer()).isTrue();
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
		assertThat(inquiry.getAnsweredAt()).isNotNull();

		// when - 사용자가 답변 확인
		inquiry.confirmAnswer();

		// then - 확인 후 상태 확인
		assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.CONFIRMED);
		assertThat(inquiry.hasAnswer()).isTrue();
	}
}
