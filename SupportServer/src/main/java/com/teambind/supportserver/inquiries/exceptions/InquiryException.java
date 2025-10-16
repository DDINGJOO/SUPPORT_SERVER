package com.teambind.supportserver.inquiries.exceptions;

import org.springframework.http.HttpStatus;

public class InquiryException extends RuntimeException {
	private final ErrorCode errorcode;

	public InquiryException(ErrorCode errorcode) {
		super(errorcode.toString());
		this.errorcode = errorcode;
	}

	public HttpStatus getStatus() {
		return errorcode.getStatus();
	}

	public ErrorCode getErrorCode() {
		return errorcode;
	}
}
