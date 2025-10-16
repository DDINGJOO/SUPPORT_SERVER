package com.teambind.supportserver.report.entity;

import com.teambind.supportserver.report.entity.embeddable.ReportCategoryId;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 신고 카테고리 엔티티
 */
@Entity
@Table(name = "report_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportCategory {

	
    @EmbeddedId
    private ReportCategoryId id;

    public static ReportCategory of(ReferenceType referenceType, String reportCategory) {
        return ReportCategory.builder()
                .id(new ReportCategoryId(referenceType, reportCategory))
                .build();
    }
}
