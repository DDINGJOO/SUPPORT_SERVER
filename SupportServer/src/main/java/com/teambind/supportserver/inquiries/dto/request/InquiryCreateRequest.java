package com.teambind.supportserver.inquiries.dto.request;

import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 문의 생성 요청 DTO
 *
 * 이미지 정보는 카프카를 통해 별도로 처리되므로 현재는 제외
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryCreateRequest {

	@NotBlank(message = "문의 제목은 필수입니다")
	@Size(max = 200, message = "문의 제목은 최대 200자까지 입력 가능합니다")
	private String title;

	@NotBlank(message = "문의 내용은 필수입니다")
	@Size(max = 2000, message = "문의 내용은 최대 2000자까지 입력 가능합니다")
	private String contents;

	@NotNull(message = "문의 카테고리는 필수입니다")
	private InquiryCategory category;

	@NotBlank(message = "작성자 ID는 필수입니다")
	@Size(max = 64, message = "작성자 ID는 최대 64자까지 입력 가능합니다")
	private String writerId;
}
