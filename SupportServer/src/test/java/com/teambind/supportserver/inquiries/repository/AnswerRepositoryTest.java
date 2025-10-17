package com.teambind.supportserver.inquiries.repository;

import com.teambind.supportserver.common.config.QueryDslConfig;
import com.teambind.supportserver.inquiries.entity.Answer;
import com.teambind.supportserver.inquiries.entity.Inquiry;
import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import com.teambind.supportserver.inquiries.entity.InquiryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * AnswerRepository 통합 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("AnswerRepository 통합 테스트")
class AnswerRepositoryTest {

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private InquiryRepository inquiryRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("답변 저장 - 정상")
	void saveAnswer_Success() {
		// given
		Inquiry inquiry = createAndSaveInquiry("결제 문의", "USER-001");

		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId("ADMIN-001")
				.contents("답변 내용입니다.")
				.build();

		// when
		Answer savedAnswer = answerRepository.save(answer);
		entityManager.flush();
		entityManager.clear();

		// then
		Answer foundAnswer = answerRepository.findById(savedAnswer.getId()).orElseThrow();
		assertThat(foundAnswer.getWriterId()).isEqualTo("ADMIN-001");
		assertThat(foundAnswer.getContents()).isEqualTo("답변 내용입니다.");
		assertThat(foundAnswer.getInquiry().getId()).isEqualTo(inquiry.getId());
	}

	@Test
	@DisplayName("답변 조회 - ID로 조회")
	void findById_Success() {
		// given
		Inquiry inquiry = createAndSaveInquiry("예약 문의", "USER-002");
		Answer answer = createAndSaveAnswer(inquiry, "ADMIN-002", "예약 취소는 마이페이지에서 가능합니다.");

		// when
		Optional<Answer> foundAnswer = answerRepository.findById(answer.getId());

		// then
		assertThat(foundAnswer).isPresent();
		assertThat(foundAnswer.get().getWriterId()).isEqualTo("ADMIN-002");
	}

	@Test
	@DisplayName("답변 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
	void findById_NotFound() {
		// when
		Optional<Answer> foundAnswer = answerRepository.findById("NON-EXISTENT-ID");

		// then
		assertThat(foundAnswer).isEmpty();
	}

	@Test
	@DisplayName("문의 ID로 답변 조회")
	void findByInquiryId_Success() {
		// given
		Inquiry inquiry = createAndSaveInquiry("기술 문의", "USER-003");
		Answer answer = createAndSaveAnswer(inquiry, "ADMIN-003", "기술 지원 답변");

		// when
		Optional<Answer> foundAnswer = answerRepository.findByInquiryId(inquiry.getId());

		// then
		assertThat(foundAnswer).isPresent();
		assertThat(foundAnswer.get().getInquiry().getId()).isEqualTo(inquiry.getId());
		assertThat(foundAnswer.get().getContents()).isEqualTo("기술 지원 답변");
	}

	@Test
	@DisplayName("문의 ID로 답변 조회 - 답변이 없는 경우")
	void findByInquiryId_NotFound() {
		// given
		Inquiry inquiry = createAndSaveInquiry("답변 없는 문의", "USER-004");

		// when
		Optional<Answer> foundAnswer = answerRepository.findByInquiryId(inquiry.getId());

		// then
		assertThat(foundAnswer).isEmpty();
	}

	@Test
	@DisplayName("작성자 ID로 답변 존재 여부 확인 - 존재하는 경우")
	void existsByWriterId_True() {
		// given
		Inquiry inquiry = createAndSaveInquiry("서비스 문의", "USER-005");
		createAndSaveAnswer(inquiry, "ADMIN-005", "서비스 답변");

		// when
		boolean exists = answerRepository.existsByWriterId("ADMIN-005");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("작성자 ID로 답변 존재 여부 확인 - 존재하지 않는 경우")
	void existsByWriterId_False() {
		// when
		boolean exists = answerRepository.existsByWriterId("NON-EXISTENT-ADMIN");

		// then
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("답변 전체 조회")
	void findAll_Success() {
		// given
		Inquiry inquiry1 = createAndSaveInquiry("문의1", "USER-001");
		Inquiry inquiry2 = createAndSaveInquiry("문의2", "USER-002");

		createAndSaveAnswer(inquiry1, "ADMIN-001", "답변1");
		createAndSaveAnswer(inquiry2, "ADMIN-002", "답변2");

		entityManager.flush();
		entityManager.clear();

		// when
		List<Answer> answers = answerRepository.findAll();

		// then
		assertThat(answers).hasSize(2);
	}

	@Test
	@DisplayName("답변 삭제 - 정상")
	void deleteAnswer_Success() {
		// given
		Inquiry inquiry = createAndSaveInquiry("삭제 테스트 문의", "USER-006");
		Answer answer = createAndSaveAnswer(inquiry, "ADMIN-006", "삭제할 답변");

		// when
		answerRepository.deleteById(answer.getId());
		entityManager.flush();
		entityManager.clear();

		// then
		Optional<Answer> deletedAnswer = answerRepository.findById(answer.getId());
		assertThat(deletedAnswer).isEmpty();
	}

	@Test
	@DisplayName("답변 수정 - 내용 변경")
	void updateAnswer_ContentChange() {
		// given
		Inquiry inquiry = createAndSaveInquiry("수정 테스트 문의", "USER-007");
		Answer answer = createAndSaveAnswer(inquiry, "ADMIN-007", "초기 답변");

		// when
		Answer foundAnswer = answerRepository.findById(answer.getId()).orElseThrow();
		Answer updatedAnswer = Answer.builder()
				.id(foundAnswer.getId())
				.inquiry(foundAnswer.getInquiry())
				.writerId(foundAnswer.getWriterId())
				.contents("수정된 답변")
				.build();
		answerRepository.save(updatedAnswer);
		entityManager.flush();
		entityManager.clear();

		// then
		Answer result = answerRepository.findById(answer.getId()).orElseThrow();
		assertThat(result.getContents()).isEqualTo("수정된 답변");
	}

	@Test
	@DisplayName("Inquiry와 Answer의 연관관계 테스트")
	void inquiryAnswerRelationship_Test() {
		// given
		Inquiry inquiry = createAndSaveInquiry("연관관계 테스트", "USER-008");
		Answer answer = createAndSaveAnswer(inquiry, "ADMIN-008", "연관관계 답변");

		// when
		Answer foundAnswer = answerRepository.findById(answer.getId()).orElseThrow();
		Inquiry foundInquiry = foundAnswer.getInquiry();

		// then
		assertThat(foundInquiry).isNotNull();
		assertThat(foundInquiry.getId()).isEqualTo(inquiry.getId());
		assertThat(foundInquiry.getTitle()).isEqualTo("연관관계 테스트");
	}

	@Test
	@DisplayName("같은 관리자가 여러 답변 작성")
	void sameAdmin_MultipleAnswers() {
		// given
		Inquiry inquiry1 = createAndSaveInquiry("문의1", "USER-009");
		Inquiry inquiry2 = createAndSaveInquiry("문의2", "USER-010");
		Inquiry inquiry3 = createAndSaveInquiry("문의3", "USER-011");

		createAndSaveAnswer(inquiry1, "ADMIN-999", "답변1");
		createAndSaveAnswer(inquiry2, "ADMIN-999", "답변2");
		createAndSaveAnswer(inquiry3, "ADMIN-999", "답변3");

		entityManager.flush();
		entityManager.clear();

		// when
		boolean exists = answerRepository.existsByWriterId("ADMIN-999");

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("다양한 카테고리의 문의에 대한 답변")
	void answers_ForDifferentCategories() {
		// given
		Inquiry paymentInquiry = createInquiry("결제 문의", InquiryCategory.PAYMENT, "USER-012");
		Inquiry technicalInquiry = createInquiry("기술 문의", InquiryCategory.REVIEW_REPORT, "USER-013");
		Inquiry serviceInquiry = createInquiry("서비스 문의", InquiryCategory.REVIEW_REPORT, "USER-014");

		inquiryRepository.saveAll(List.of(paymentInquiry, technicalInquiry, serviceInquiry));
		entityManager.flush();

		createAndSaveAnswer(paymentInquiry, "ADMIN-012", "결제 답변");
		createAndSaveAnswer(technicalInquiry, "ADMIN-013", "기술 답변");
		createAndSaveAnswer(serviceInquiry, "ADMIN-014", "서비스 답변");

		entityManager.flush();
		entityManager.clear();

		// when
		List<Answer> answers = answerRepository.findAll();

		// then
		assertThat(answers).hasSize(3);
	}

	@Test
	@DisplayName("긴 답변 내용 저장 및 조회")
	void saveAndRetrieve_LongContent() {
		// given
		String longContent = "답변 ".repeat(500); // 약 1500자
		Inquiry inquiry = createAndSaveInquiry("긴 답변 필요한 문의", "USER-015");
		Answer answer = createAndSaveAnswer(inquiry, "ADMIN-015", longContent);

		// when
		Answer foundAnswer = answerRepository.findById(answer.getId()).orElseThrow();

		// then
		assertThat(foundAnswer.getContents()).isEqualTo(longContent);
		assertThat(foundAnswer.getContents()).hasSizeGreaterThan(1000);
	}

	private Inquiry createAndSaveInquiry(String title, String writerId) {
		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title(title)
				.contents("문의 내용")
				.category(InquiryCategory.ETC)
				.status(InquiryStatus.UNANSWERED)
				.writerId(writerId)
				.files(new ArrayList<>())
				.build();
		return inquiryRepository.save(inquiry);
	}

	private Inquiry createInquiry(String title, InquiryCategory category, String writerId) {
		return Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title(title)
				.contents("문의 내용")
				.category(category)
				.status(InquiryStatus.UNANSWERED)
				.writerId(writerId)
				.files(new ArrayList<>())
				.build();
	}

	private Answer createAndSaveAnswer(Inquiry inquiry, String writerId, String contents) {
		Answer answer = Answer.builder()
				.id(UUID.randomUUID().toString())
				.inquiry(inquiry)
				.writerId(writerId)
				.contents(contents)
				.build();
		return answerRepository.save(answer);
	}
}
