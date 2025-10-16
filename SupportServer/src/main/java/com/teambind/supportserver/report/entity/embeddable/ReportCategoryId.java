package com.teambind.supportserver.report.entity.embeddable;

import com.teambind.supportserver.report.entity.enums.ReferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * 신고 카테고리 복합키
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportCategoryId implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    @Column(name = "report_category")
    private String reportCategory;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportCategoryId that = (ReportCategoryId) o;
        return referenceType == that.referenceType &&
                Objects.equals(reportCategory, that.reportCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceType, reportCategory);
    }
}
