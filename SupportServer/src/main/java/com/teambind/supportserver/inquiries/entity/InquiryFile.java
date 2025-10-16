package com.teambind.supportserver.inquiries.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryFile {
	// 이미지 서버 요구사항 반영
	@Column(name = "image_id", nullable = false, length = 64)
	private String imageId;
	
	@Column(name = "image_url", nullable = false, length = 512)
	private String imageUrl;
	
	// 필요 시 원본 파일명
	@Column(name = "file_name", nullable = false, length = 200)
	private String fileName;
}

