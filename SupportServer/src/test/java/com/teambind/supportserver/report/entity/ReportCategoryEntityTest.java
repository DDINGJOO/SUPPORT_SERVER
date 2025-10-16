package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ReportCategory 엔티티 단위 테스트
 */
@DisplayName("ReportCategory 엔티티 테스트")
class ReportCategoryEntityTest {

    @Test
    @DisplayName("ReportCategory 엔티티 생성 - 정상")
    void createReportCategory_Success() {
        // given
        ReportCategoryId id = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");

        // when
        ReportCategory category = ReportCategory.builder()
                .id(id)
                .build();

        // then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getId().getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(category.getId().getReportCategory()).isEqualTo("SPAM");
    }

    @Test
    @DisplayName("ReportCategory.of() - 정적 팩토리 메서드로 생성")
    void createReportCategory_UsingOfMethod() {
        // when
        ReportCategory category = ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE");

        // then
        assertThat(category).isNotNull();
        assertThat(category.getId().getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(category.getId().getReportCategory()).isEqualTo("INAPPROPRIATE");
    }

    @Test
    @DisplayName("ReportCategory - PROFILE 타입 카테고리들")
    void createReportCategory_ProfileTypes() {
        // when
        ReportCategory spam = ReportCategory.of(ReferenceType.PROFILE, "SPAM");
        ReportCategory abuse = ReportCategory.of(ReferenceType.PROFILE, "ABUSE");
        ReportCategory fake = ReportCategory.of(ReferenceType.PROFILE, "FAKE");

        // then
        assertThat(spam.getId().getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(abuse.getId().getReferenceType()).isEqualTo(ReferenceType.PROFILE);
        assertThat(fake.getId().getReferenceType()).isEqualTo(ReferenceType.PROFILE);

        assertThat(spam.getId().getReportCategory()).isEqualTo("SPAM");
        assertThat(abuse.getId().getReportCategory()).isEqualTo("ABUSE");
        assertThat(fake.getId().getReportCategory()).isEqualTo("FAKE");
    }

    @Test
    @DisplayName("ReportCategory - ARTICLE 타입 카테고리들")
    void createReportCategory_ArticleTypes() {
        // when
        ReportCategory inappropriate = ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE");
        ReportCategory violence = ReportCategory.of(ReferenceType.ARTICLE, "VIOLENCE");
        ReportCategory hate = ReportCategory.of(ReferenceType.ARTICLE, "HATE_SPEECH");

        // then
        assertThat(inappropriate.getId().getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(violence.getId().getReferenceType()).isEqualTo(ReferenceType.ARTICLE);
        assertThat(hate.getId().getReferenceType()).isEqualTo(ReferenceType.ARTICLE);

        assertThat(inappropriate.getId().getReportCategory()).isEqualTo("INAPPROPRIATE");
        assertThat(violence.getId().getReportCategory()).isEqualTo("VIOLENCE");
        assertThat(hate.getId().getReportCategory()).isEqualTo("HATE_SPEECH");
    }

    @Test
    @DisplayName("ReportCategory - BUSINESS 타입 카테고리들")
    void createReportCategory_BusinessTypes() {
        // when
        ReportCategory fraud = ReportCategory.of(ReferenceType.BUSINESS, "FRAUD");
        ReportCategory scam = ReportCategory.of(ReferenceType.BUSINESS, "SCAM");

        // then
        assertThat(fraud.getId().getReferenceType()).isEqualTo(ReferenceType.BUSINESS);
        assertThat(scam.getId().getReferenceType()).isEqualTo(ReferenceType.BUSINESS);

        assertThat(fraud.getId().getReportCategory()).isEqualTo("FRAUD");
        assertThat(scam.getId().getReportCategory()).isEqualTo("SCAM");
    }

    @Test
    @DisplayName("ReportCategoryId - equals 테스트")
    void reportCategoryId_EqualsTest() {
        // given
        ReportCategoryId id1 = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");
        ReportCategoryId id2 = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");
        ReportCategoryId id3 = new ReportCategoryId(ReferenceType.PROFILE, "ABUSE");

        // when & then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
    }

    @Test
    @DisplayName("ReportCategoryId - hashCode 테스트")
    void reportCategoryId_HashCodeTest() {
        // given
        ReportCategoryId id1 = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");
        ReportCategoryId id2 = new ReportCategoryId(ReferenceType.PROFILE, "SPAM");

        // when & then
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("ReportCategory - 다양한 ReferenceType과 카테고리 조합")
    void reportCategory_VariousCombinations() {
        // when
        ReportCategory[] categories = {
                ReportCategory.of(ReferenceType.PROFILE, "SPAM"),
                ReportCategory.of(ReferenceType.PROFILE, "ABUSE"),
                ReportCategory.of(ReferenceType.PROFILE, "FAKE"),
                ReportCategory.of(ReferenceType.ARTICLE, "INAPPROPRIATE"),
                ReportCategory.of(ReferenceType.ARTICLE, "VIOLENCE"),
                ReportCategory.of(ReferenceType.BUSINESS, "FRAUD")
        };

        // then
        assertThat(categories).hasSize(6);
        assertThat(categories).allMatch(c -> c.getId() != null);
        assertThat(categories).allMatch(c -> c.getId().getReferenceType() != null);
        assertThat(categories).allMatch(c -> c.getId().getReportCategory() != null);
    }
}
