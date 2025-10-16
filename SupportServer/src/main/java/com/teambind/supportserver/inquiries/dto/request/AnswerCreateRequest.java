package com.teambind.supportserver.inquiries.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 답변 생성 요청 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerCreateRequest {

	@NotBlank(message = "문의 ID는 필수입니다")
	private String inquiryId;

	@NotBlank(message = "답변 내용은 필수입니다")
	@Size(max = 2000, message = "답변 내용은 최대 2000자까지 입력 가능합니다")
	private String contents;

	@NotBlank(message = "답변 작성자 ID는 필수입니다")
	@Size(max = 64, message = "답변 작성자 ID는 최대 64자까지 입력 가능합니다")
	private String writerId;
}
