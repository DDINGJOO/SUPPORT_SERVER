package com.teambind.supportserver.inquiries.dto.response;

import com.teambind.supportserver.inquiries.entity.Inquiry;
import com.teambind.supportserver.inquiries.entity.InquiryCategory;
import com.teambind.supportserver.inquiries.entity.InquiryStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 문의 응답 DTO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponse {

	private String id;
	private String title;
	private String contents;
	private InquiryCategory category;
	private InquiryStatus status;
	private String writerId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime answeredAt;
	private boolean hasAnswer;

	// 답변 정보 (답변이 있는 경우)
	private AnswerResponse answer;

	/**
	 * Entity -> DTO 변환
	 */
	public static InquiryResponse from(Inquiry inquiry) {
		InquiryResponseBuilder builder = InquiryResponse.builder()
				.id(inquiry.getId())
				.title(inquiry.getTitle())
				.contents(inquiry.getContents())
				.category(inquiry.getCategory())
				.status(inquiry.getStatus())
				.writerId(inquiry.getWriterId())
				.createdAt(inquiry.getCreatedAt())
				.updatedAt(inquiry.getUpdatedAt())
				.answeredAt(inquiry.getAnsweredAt())
				.hasAnswer(inquiry.hasAnswer());

		// 답변이 있으면 답변 정보도 포함
		if (inquiry.hasAnswer()) {
			builder.answer(AnswerResponse.from(inquiry.getAnswer()));
		}

		return builder.build();
	}

	/**
	 * Entity -> DTO 변환 (답변 정보 제외)
	 */
	public static InquiryResponse fromWithoutAnswer(Inquiry inquiry) {
		return InquiryResponse.builder()
				.id(inquiry.getId())
				.title(inquiry.getTitle())
				.contents(inquiry.getContents())
				.category(inquiry.getCategory())
				.status(inquiry.getStatus())
				.writerId(inquiry.getWriterId())
				.createdAt(inquiry.getCreatedAt())
				.updatedAt(inquiry.getUpdatedAt())
				.answeredAt(inquiry.getAnsweredAt())
				.hasAnswer(inquiry.hasAnswer())
				.build();
	}
}
