package com.teambind.supportserver.inquiries.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Inquiry {
	
	@Id
	@Column(name = "inquiry_id", length = 36)
	private String id; // UUID 문자열
	
	@Column(nullable = false, length = 200)
	private String title;
	
	@Column(nullable = false, length = 2000)
	private String contents;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private InquiryCategory category;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InquiryStatus status;
	
	@Column(name = "writer_id", nullable = false, length = 64)
	private String writerId;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	
	@Column(name = "answered_at")
	private LocalDateTime answeredAt;

	// Answer와의 양방향 관계
	@OneToOne(mappedBy = "inquiry", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Answer answer;

	// 임베디드 컬렉션 테이블
	@ElementCollection
	@CollectionTable(
			name = "inquiry_files",
			joinColumns = @JoinColumn(name = "inquiry_id")
	)
	@OrderColumn(name = "order_num") // 0..4
	private List<InquiryFile> files = new ArrayList<>();
	
	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		validateFiles();
	}
	
	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
		validateFiles();
	}
	
	private void validateFiles() {
		if (files != null && files.size() > 5) {
			throw new IllegalArgumentException("파일은 최대 5개까지 첨부할 수 있습니다.");
		}
	}

	// 연관관계 편의 메소드
	/**
	 * 답변 등록 (양방향 연관관계 설정)
	 * 답변이 등록되면 문의 상태를 ANSWERED로 변경하고 answeredAt 시간을 설정합니다.
	 */
	public void addAnswer(Answer answer) {
		if (this.answer != null) {
			throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
		}
		this.answer = answer;
		this.status = InquiryStatus.ANSWERED;
		this.answeredAt = LocalDateTime.now();
	}

	/**
	 * 답변 삭제 (양방향 연관관계 해제)
	 */
	public void removeAnswer() {
		if (this.answer == null) {
			throw new IllegalStateException("등록된 답변이 없습니다.");
		}
		this.answer = null;
		this.status = InquiryStatus.UNANSWERED;
		this.answeredAt = null;
	}

	/**
	 * 사용자가 답변을 확인했을 때 상태를 CONFIRMED로 변경
	 */
	public void confirmAnswer() {
		if (this.status != InquiryStatus.ANSWERED) {
			throw new IllegalStateException("답변이 완료된 문의만 확인할 수 있습니다.");
		}
		this.status = InquiryStatus.CONFIRMED;
	}

	/**
	 * 답변 존재 여부 확인
	 */
	public boolean hasAnswer() {
		return this.answer != null;
	}
}
