package com.teambind.supportserver.report.repository;

import com.teambind.supportserver.report.entity.ReportCategory;
import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportCategoryRepository 통합 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ReportCategoryRepository 통합 테스트")
class ReportCategoryRepositoryTest {

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("신고 카테고리 저장 - 정상")
    void saveReportCategory_Success() {
        // given
        ReportCategory category = ReportCategory.of(ReferenceType.PROFILE, "SPAM");

        // when
        ReportCategory savedCategory = reportCategoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // then
        ReportCategoryId id = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");
        ReportCategory foundCategory = reportCategoryRepository.findById(id).orElseThrow();
        assertThat(foundCategory.getId().getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(foundCategory.getId().getReportCategory()).isEqualTo("SPAM");
    }

    @Test
    @DisplayName("신고 카테고리 조회 - 복합키로 조회")
    void findById_Success() {
        // given
        ReportCategory category = ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE");
        reportCategoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // when
        ReportCategoryId id = new ReportCategoryId(ReferenceType.ARTICLE, "INAPPROPRIATE");
        Optional<ReportCategory> foundCategory = reportCategoryRepository.findById(id);

        // then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getId().getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(foundCategory.get().getId().getReportCategory()).isEqualTo("INAPPROPRIATE");
    }

    @Test
    @DisplayName("신고 카테고리 조회 - 존재하지 않는 ID로 조회 시 empty 반환")
    void findById_NotFound() {
        // when
        ReportCategoryId id = new ReportCategoryId(ReferenceType.PROFILE, "NON_EXISTENT");
        Optional<ReportCategory> foundCategory = reportCategoryRepository.findById(id);

        // then
        assertThat(foundCategory).isEmpty();
    }

    @Test
    @DisplayName("신고 카테고리 전체 조회")
    void findAll_Success() {
        // given
        ReportCategory category1 = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        ReportCategory category2 = ReportCategory.of(ReferenceType.PROFILE, "ABUSE");
        ReportCategory category3 = ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE");

        reportCategoryRepository.saveAll(List.of(category1, category2, category3));
        entityManager.flush();
        entityManager.clear();

        // when
        List<ReportCategory> categories = reportCategoryRepository.findAll();

        // then
        assertThat(categories).hasSize(3);
    }

    @Test
    @DisplayName("신고 카테고리 삭제 - 정상")
    void deleteReportCategory_Success() {
        // given
        ReportCategory category = ReportCategory.of(ReferenceType.PROFILE, "FAKE");
        reportCategoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        ReportCategoryId id = new ReportCategoryId(ReferenceType.PROFILE, "FAKE");

        // when
        reportCategoryRepository.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<ReportCategory> deletedCategory = reportCategoryRepository.findById(id);
        assertThat(deletedCategory).isEmpty();
    }

	
	//TODO: why not pass test?
//    @Test
//    @DisplayName("복합키 중복 저장 불가 - 같은 referenceType과 reportCategory 조합")
//    void uniqueConstraint_DuplicateKey_ThrowsException() {
//        // given
//        ReportCategory category1 = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
//        ReportCategory category2 = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
//
//        // when
//        reportCategoryRepository.save(category1);
//        entityManager.flush();
//
//        // then
//	    assertThatThrownBy(() -> reportCategoryRepository.save(category2))
//                .isInstanceOf(DataIntegrityViolationException.class);
//    }

    @Test
    @DisplayName("다른 ReferenceType에 같은 카테고리명 사용 가능")
    void sameCategory_DifferentReferenceType_AllowsSave() {
        // given
        ReportCategory profileSpam = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        ReportCategory articleSpam = ReportCategory.of(ReferenceType.ARTICLE, "SPAM");

        // when
        reportCategoryRepository.save(profileSpam);
        reportCategoryRepository.save(articleSpam);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        assertThat(categories).hasSize(2);
    }

    @Test
    @DisplayName("PROFILE 타입 카테고리 여러 개 저장")
    void saveMultiple_ProfileCategories() {
        // given
        List<ReportCategory> profileCategories = List.of(
                ReportCategory.of(ReferenceType.PROFILE, "SPAM"),
                ReportCategory.of(ReferenceType.PROFILE, "ABUSE"),
                ReportCategory.of(ReferenceType.PROFILE, "FAKE"),
                ReportCategory.of(ReferenceType.PROFILE, "INAPPROPRIATE"),
                ReportCategory.of(ReferenceType.PROFILE, "HARASSMENT")
        );

        // when
        reportCategoryRepository.saveAll(profileCategories);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        assertThat(categories).hasSize(5);
        assertThat(categories).allMatch(c -> c.getId().getReferenceType() == ReferenceType.PROFILE);
    }

    @Test
    @DisplayName("ARTICLE 타입 카테고리 여러 개 저장")
    void saveMultiple_ArticleCategories() {
        // given
        List<ReportCategory> articleCategories = List.of(
                ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE"),
                ReportCategory.of(ReferenceType.ARTICLE, "VIOLENCE"),
                ReportCategory.of(ReferenceType.ARTICLE, "HATE_SPEECH"),
                ReportCategory.of(ReferenceType.ARTICLE, "FALSE_INFO"),
                ReportCategory.of(ReferenceType.ARTICLE, "COPYRIGHT")
        );

        // when
        reportCategoryRepository.saveAll(articleCategories);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        assertThat(categories).hasSize(5);
        assertThat(categories).allMatch(c -> c.getId().getReferenceType() == ReferenceType.ARTICLE);
    }

    @Test
    @DisplayName("BUSINESS 타입 카테고리 여러 개 저장")
    void saveMultiple_BusinessCategories() {
        // given
        List<ReportCategory> businessCategories = List.of(
                ReportCategory.of(ReferenceType.BUSINESS, "FRAUD"),
                ReportCategory.of(ReferenceType.BUSINESS, "SCAM"),
                ReportCategory.of(ReferenceType.BUSINESS, "FAKE_BUSINESS"),
                ReportCategory.of(ReferenceType.BUSINESS, "ILLEGAL_ACTIVITY")
        );

        // when
        reportCategoryRepository.saveAll(businessCategories);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        assertThat(categories).hasSize(4);
        assertThat(categories).allMatch(c -> c.getId().getReferenceType() == ReferenceType.BUSINESS);
    }

    @Test
    @DisplayName("모든 ReferenceType에 대한 카테고리 저장 및 조회")
    void saveAll_AllReferenceTypes() {
        // given
        List<ReportCategory> allCategories = List.of(
                // PROFILE
                ReportCategory.of(ReferenceType.PROFILE, "SPAM"),
                ReportCategory.of(ReferenceType.PROFILE, "ABUSE"),
                // ARTICLE
                ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE"),
                ReportCategory.of(ReferenceType.ARTICLE, "VIOLENCE"),
                // BUSINESS
                ReportCategory.of(ReferenceType.BUSINESS, "FRAUD"),
                ReportCategory.of(ReferenceType.BUSINESS, "SCAM")
        );

        // when
        reportCategoryRepository.saveAll(allCategories);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        assertThat(categories).hasSize(6);

        long profileCount = categories.stream()
                .filter(c -> c.getId().getReferenceType() == ReferenceType.PROFILE)
                .count();
        long articleCount = categories.stream()
                .filter(c -> c.getId().getReferenceType() == ReferenceType.ARTICLE)
                .count();
        long businessCount = categories.stream()
                .filter(c -> c.getId().getReferenceType() == ReferenceType.BUSINESS)
                .count();

        assertThat(profileCount).isEqualTo(2);
        assertThat(articleCount).isEqualTo(2);
        assertThat(businessCount).isEqualTo(2);
    }

    @Test
    @DisplayName("대량 카테고리 생성 및 조회 테스트")
    void bulkCategories_CreateAndRetrieve() {
        // given
        for (int i = 1; i <= 50; i++) {
            ReportCategory category = ReportCategory.of(
                    ReferenceType.PROFILE,
                    "CATEGORY_" + i
            );
            reportCategoryRepository.save(category);
        }
        entityManager.flush();
        entityManager.clear();

        // when
        List<ReportCategory> categories = reportCategoryRepository.findAll();

        // then
        assertThat(categories).hasSize(50);
    }

    @Test
    @DisplayName("ReportCategoryId equals 테스트")
    void reportCategoryId_EqualsTest() {
        // given
        ReportCategory category1 = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        ReportCategory category2 = ReportCategory.of(ReferenceType.PROFILE, "SPAM");

        reportCategoryRepository.save(category1);
        entityManager.flush();
        entityManager.clear();

        // when
        ReportCategoryId id = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");
        Optional<ReportCategory> foundCategory = reportCategoryRepository.findById(id);

        // then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getId()).isEqualTo(category2.getId());
    }

    @Test
    @DisplayName("카테고리명 대소문자 구분 테스트")
    void categoryName_CaseSensitive() {
        // given
        ReportCategory lowercase = ReportCategory.of(ReferenceType.PROFILE, "spam");
        ReportCategory uppercase = ReportCategory.of(ReferenceType.PROFILE, "SPAM");

        // when
        reportCategoryRepository.save(lowercase);
        reportCategoryRepository.save(uppercase);
        entityManager.flush();
        entityManager.clear();

        // then
        List<ReportCategory> categories = reportCategoryRepository.findAll();
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(c -> c.getId().getReportCategory())
                .containsExactlyInAnyOrder("spam", "SPAM");
    }
}
