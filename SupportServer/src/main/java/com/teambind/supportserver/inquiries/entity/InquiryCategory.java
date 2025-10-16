package com.teambind.supportserver.inquiries.entity;

/**
 * 문의 카테고리 (FAQ 카테고리와 동일)
 */
public enum InquiryCategory {
	ALL,            // 전체
	RESERVATION,    // 예약 관련
	CHECK_IN,       // 이용/입실
	PAYMENT,        // 요금/결제
	REVIEW_REPORT,  // 리뷰/신고
	ETC             // 기타
}