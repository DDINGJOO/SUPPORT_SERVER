package com.teambind.supportserver.inquiries.repository;

import com.teambind.supportserver.inquiries.entity.Inquiry;
import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import com.teambind.supportserver.inquiries.entity.InquiryFile;
import com.teambind.supportserver.inquiries.entity.InquiryStatus;
import com.teambind.supportserver.common.config.QueryDslConfig;
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
 * InquiryRepository 통합 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("InquiryRepository 통합 테스트")
class InquiryRepositoryTest {

	@Autowired
	private InquiryRepository inquiryRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("문의 저장 - 정상")
	void saveInquiry_Success() {
		// given
		List<InquiryFile> files = new ArrayList<>();
		files.add(InquiryFile.builder()
				.imageId("IMG-001")
				.imageUrl("https://example.com/image1.jpg")
				.fileName("test1.jpg")
				.build());

		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("결제 문의")
				.contents("결제가 안 됩니다.")
				.category(InquiryCategory.PAYMENT)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-001")
				.files(files)
				.build();

		// when
		Inquiry savedInquiry = inquiryRepository.save(inquiry);
		entityManager.flush();
		entityManager.clear();

		// then
		Inquiry foundInquiry = inquiryRepository.findById(savedInquiry.getId()).orElseThrow();
		assertThat(foundInquiry.getTitle()).isEqualTo("결제 문의");
		assertThat(foundInquiry.getContents()).isEqualTo("결제가 안 됩니다.");
		assertThat(foundInquiry.getCategory()).isEqualTo(InquiryCategory.PAYMENT);
		assertThat(foundInquiry.getStatus()).isEqualTo(InquiryStatus.UNANSWERED);
		assertThat(foundInquiry.getFiles()).hasSize(1);
	}

	@Test
	@DisplayName("문의 조회 - ID로 조회")
	void findById_Success() {
		// given
		Inquiry inquiry = createInquiry("예약 문의", "예약을 취소하고 싶습니다.",
				InquiryCategory.RESERVATION, "USER-002");
		inquiryRepository.save(inquiry);
		entityManager.flush();
		entityManager.clear();

		// when
		Optional<Inquiry> foundInquiry = inquiryRepository.findById(inquiry.getId());

		// then
		assertThat(foundInquiry).isPresent();
		assertThat(foundInquiry.get().getTitle()).isEqualTo("예약 문의");
	}

	@Test
	@DisplayName("문의 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
	void findById_NotFound() {
		// when
		Optional<Inquiry> foundInquiry = inquiryRepository.findById("NON-EXISTENT-ID");

		// then
		assertThat(foundInquiry).isEmpty();
	}

	@Test
	@DisplayName("작성자 ID로 문의 목록 조회")
	void findByWriterId_Success() {
		// given
		Inquiry inquiry1 = createInquiry("문의1", "내용1", InquiryCategory.PAYMENT, "USER-001");
		Inquiry inquiry2 = createInquiry("문의2", "내용2", InquiryCategory.PAYMENT, "USER-001");
		Inquiry inquiry3 = createInquiry("문의3", "내용3", InquiryCategory.CHECK_IN, "USER-002");

		inquiryRepository.saveAll(List.of(inquiry1, inquiry2, inquiry3));
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> inquiries = inquiryRepository.findByWriterId("USER-001");

		// then
		assertThat(inquiries).hasSize(2);
		assertThat(inquiries).extracting(Inquiry::getWriterId)
				.containsOnly("USER-001");
	}

	@Test
	@DisplayName("카테고리별 문의 목록 조회")
	void findByCategory_Success() {
		// given
		Inquiry inquiry1 = createInquiry("결제 문의1", "내용1", InquiryCategory.PAYMENT, "USER-001");
		Inquiry inquiry2 = createInquiry("결제 문의2", "내용2", InquiryCategory.PAYMENT, "USER-002");
		Inquiry inquiry3 = createInquiry("기술 문의", "내용3", InquiryCategory.ETC, "USER-003");

		inquiryRepository.saveAll(List.of(inquiry1, inquiry2, inquiry3));
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> paymentInquiries = inquiryRepository.findByCategory(InquiryCategory.PAYMENT);

		// then
		assertThat(paymentInquiries).hasSize(2);
		assertThat(paymentInquiries).extracting(Inquiry::getCategory)
				.containsOnly(InquiryCategory.PAYMENT);
	}

	@Test
	@DisplayName("상태별 문의 목록 조회")
	void findByStatus_Success() {
		// given
		Inquiry inquiry1 = createInquiryWithStatus("문의1", InquiryStatus.UNANSWERED, "USER-001");
		Inquiry inquiry2 = createInquiryWithStatus("문의2", InquiryStatus.UNANSWERED, "USER-002");
		Inquiry inquiry3 = createInquiryWithStatus("문의3", InquiryStatus.ANSWERED, "USER-003");

		inquiryRepository.saveAll(List.of(inquiry1, inquiry2, inquiry3));
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> unansweredInquiries = inquiryRepository.findByStatus(InquiryStatus.UNANSWERED);

		// then
		assertThat(unansweredInquiries).hasSize(2);
		assertThat(unansweredInquiries).extracting(Inquiry::getStatus)
				.containsOnly(InquiryStatus.UNANSWERED);
	}

	@Test
	@DisplayName("작성자 ID와 상태로 문의 목록 조회")
	void findByWriterIdAndStatus_Success() {
		// given
		Inquiry inquiry1 = createInquiryWithStatus("문의1", InquiryStatus.UNANSWERED, "USER-001");
		Inquiry inquiry2 = createInquiryWithStatus("문의2", InquiryStatus.ANSWERED, "USER-001");
		Inquiry inquiry3 = createInquiryWithStatus("문의3", InquiryStatus.UNANSWERED, "USER-002");

		inquiryRepository.saveAll(List.of(inquiry1, inquiry2, inquiry3));
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> inquiries = inquiryRepository.findByWriterIdAndStatus("USER-001", InquiryStatus.UNANSWERED);

		// then
		assertThat(inquiries).hasSize(1);
		assertThat(inquiries.get(0).getWriterId()).isEqualTo("USER-001");
		assertThat(inquiries.get(0).getStatus()).isEqualTo(InquiryStatus.UNANSWERED);
	}

	@Test
	@DisplayName("문의 전체 조회")
	void findAll_Success() {
		// given
		Inquiry inquiry1 = createInquiry("문의1", "내용1", InquiryCategory.PAYMENT, "USER-001");
		Inquiry inquiry2 = createInquiry("문의2", "내용2", InquiryCategory.REVIEW_REPORT, "USER-002");

		inquiryRepository.saveAll(List.of(inquiry1, inquiry2));
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> inquiries = inquiryRepository.findAll();

		// then
		assertThat(inquiries).hasSize(2);
	}

	@Test
	@DisplayName("문의 삭제 - 정상")
	void deleteInquiry_Success() {
		// given
		Inquiry inquiry = createInquiry("삭제 테스트", "삭제할 문의",
				InquiryCategory.ETC, "USER-001");
		inquiryRepository.save(inquiry);
		entityManager.flush();
		entityManager.clear();

		// when
		inquiryRepository.deleteById(inquiry.getId());
		entityManager.flush();
		entityManager.clear();

		// then
		Optional<Inquiry> deletedInquiry = inquiryRepository.findById(inquiry.getId());
		assertThat(deletedInquiry).isEmpty();
	}

	@Test
	@DisplayName("문의 수정 - 상태 변경")
	void updateInquiry_StatusChange() {
		// given
		Inquiry inquiry = createInquiryWithStatus("문의", InquiryStatus.UNANSWERED, "USER-001");
		inquiryRepository.save(inquiry);
		entityManager.flush();
		entityManager.clear();

		// when
		Inquiry foundInquiry = inquiryRepository.findById(inquiry.getId()).orElseThrow();
		Inquiry updatedInquiry = Inquiry.builder()
				.id(foundInquiry.getId())
				.title(foundInquiry.getTitle())
				.contents(foundInquiry.getContents())
				.category(foundInquiry.getCategory())
				.status(InquiryStatus.ANSWERED)
				.writerId(foundInquiry.getWriterId())
				.files(foundInquiry.getFiles())
				.build();
		inquiryRepository.save(updatedInquiry);
		entityManager.flush();
		entityManager.clear();

		// then
		Inquiry result = inquiryRepository.findById(inquiry.getId()).orElseThrow();
		assertThat(result.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
	}

	@Test
	@DisplayName("다양한 InquiryCategory로 문의 저장 및 조회")
	void saveInquiries_WithDifferentCategories() {
		// given
		List<Inquiry> inquiries = new ArrayList<>();
		for (InquiryCategory category : InquiryCategory.values()) {
			if (category != InquiryCategory.ALL) {
				inquiries.add(createInquiry("문의: " + category, "내용", category, "USER-001"));
			}
		}

		inquiryRepository.saveAll(inquiries);
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> savedInquiries = inquiryRepository.findAll();

		// then
		assertThat(savedInquiries).hasSizeGreaterThanOrEqualTo(inquiries.size());
	}

	@Test
	@DisplayName("다양한 InquiryStatus로 문의 저장 및 조회")
	void saveInquiries_WithDifferentStatuses() {
		// given
		Inquiry unanswered = createInquiryWithStatus("미확인", InquiryStatus.UNANSWERED, "USER-001");
		Inquiry answered = createInquiryWithStatus("답변완료", InquiryStatus.ANSWERED, "USER-002");
		Inquiry confirmed = createInquiryWithStatus("확인완료", InquiryStatus.CONFIRMED, "USER-003");

		inquiryRepository.saveAll(List.of(unanswered, answered, confirmed));
		entityManager.flush();
		entityManager.clear();

		// when
		List<Inquiry> inquiries = inquiryRepository.findAll();

		// then
		assertThat(inquiries).hasSize(3);
		assertThat(inquiries).extracting(Inquiry::getStatus)
				.containsExactlyInAnyOrder(
						InquiryStatus.UNANSWERED,
						InquiryStatus.ANSWERED,
						InquiryStatus.CONFIRMED
				);
	}

	@Test
	@DisplayName("파일 첨부된 문의 저장 및 조회")
	void saveInquiry_WithFiles() {
		// given
		List<InquiryFile> files = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			files.add(InquiryFile.builder()
					.imageId("IMG-" + i)
					.imageUrl("https://example.com/image" + i + ".jpg")
					.fileName("test" + i + ".jpg")
					.build());
		}

		Inquiry inquiry = Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title("파일 첨부 문의")
				.contents("파일을 첨부했습니다.")
				.category(InquiryCategory.CHECK_IN)
				.status(InquiryStatus.UNANSWERED)
				.writerId("USER-001")
				.files(files)
				.build();

		inquiryRepository.save(inquiry);
		entityManager.flush();
		entityManager.clear();

		// when
		Inquiry foundInquiry = inquiryRepository.findById(inquiry.getId()).orElseThrow();

		// then
		assertThat(foundInquiry.getFiles()).hasSize(3);
		assertThat(foundInquiry.getFiles()).extracting(InquiryFile::getFileName)
				.containsExactly("test1.jpg", "test2.jpg", "test3.jpg");
	}

	private Inquiry createInquiry(String title, String contents, InquiryCategory category, String writerId) {
		return Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title(title)
				.contents(contents)
				.category(category)
				.status(InquiryStatus.UNANSWERED)
				.writerId(writerId)
				.files(new ArrayList<>())
				.build();
	}

	private Inquiry createInquiryWithStatus(String title, InquiryStatus status, String writerId) {
		return Inquiry.builder()
				.id(UUID.randomUUID().toString())
				.title(title)
				.contents("테스트 내용")
				.category(InquiryCategory.ETC)
				.status(status)
				.writerId(writerId)
				.files(new ArrayList<>())
				.build();
	}
}
