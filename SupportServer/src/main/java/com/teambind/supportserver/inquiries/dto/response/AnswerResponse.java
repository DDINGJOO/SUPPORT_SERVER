package com.teambind.supportserver.inquiries.dto.response;

import com.teambind.supportserver.inquiries.entity.Answer;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 답변 응답 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponse {

	private String id;
	private String inquiryId;
	private String writerId;
	private String contents;
	private LocalDateTime createdAt;

	/**
	 * Entity -> DTO 변환
	 */
	public static AnswerResponse from(Answer answer) {
		return AnswerResponse.builder()
				.id(answer.getId())
				.inquiryId(answer.getInquiry().getId())
				.writerId(answer.getWriterId())
				.contents(answer.getContents())
				.createdAt(answer.getCreatedAt())
				.build();
	}
}
