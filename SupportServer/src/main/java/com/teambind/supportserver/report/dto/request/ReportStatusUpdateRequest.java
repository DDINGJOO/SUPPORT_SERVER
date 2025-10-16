package com.teambind.supportserver.report.dto.request;

import com.teambind.supportserver.report.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 신고 상태 변경 요청 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStatusUpdateRequest {

    @NotNull(message = "변경할 상태는 필수입니다")
    private ReportStatus status;

    @NotBlank(message = "관리자 ID는 필수입니다")
    private String adminId;

    @Size(max = 500, message = "코멘트는 최대 500자까지 입력 가능합니다")
    private String comment;
}
