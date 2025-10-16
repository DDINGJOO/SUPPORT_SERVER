package com.teambind.supportserver.inquiries.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Answer 엔티티 (1:1)
@Entity
@Table(name = "answers",
		uniqueConstraints = @UniqueConstraint(columnNames = "inquiry_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Answer {
	
	@Id
	@Column(name = "answer_id", length = 36)
	private String id; // UUID
	
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "inquiry_id", nullable = false)
	private Inquiry inquiry;
	
	@Column(name = "writer_id", nullable = false, length = 64)
	private String writerId;
	
	@Column(nullable = false, length = 2000)
	private String contents;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
		// 답변 생성 시 문의 상태/answeredAt 갱신 로직은 서비스 계층에서 트랜잭션 내 처리 권장
	}
}
