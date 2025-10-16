package com.teambind.supportserver.report.dto.request;


import com.teambind.supportserver.report.entity.enums.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 신고 등록 요청 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotBlank(message = "신고자 ID는 필수입니다")
    private String reporterId;

    @NotBlank(message = "신고 대상 ID는 필수입니다")
    private String reportedId;

    @NotNull(message = "신고 대상 타입은 필수입니다")
    private ReferenceType referenceType;

    @NotBlank(message = "신고 카테고리는 필수입니다")
    private String reportCategory;

    @NotBlank(message = "신고 사유는 필수입니다")
    @Size(max = 500, message = "신고 사유는 최대 500자까지 입력 가능합니다")
    private String reason;
}
