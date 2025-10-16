package com.teambind.supportserver.inquiries.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	INQUIRY_NOT_FOUND("INQUIRY_NOT_FOUND", "Inquiry Not Found", HttpStatus.NOT_FOUND),
	ANSWER_NOT_FOUND("ANSWER_NOT_FOUND", "Answer Not Found", HttpStatus.NOT_FOUND),
	ANSWER_ALREADY_EXISTS("ANSWER_ALREADY_EXISTS", "Answer Already Exists", HttpStatus.CONFLICT),
	UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "Unauthorized Access", HttpStatus.FORBIDDEN),
	INVALID_INQUIRY_STATUS("INVALID_INQUIRY_STATUS", "Invalid Inquiry Status", HttpStatus.BAD_REQUEST),
	;

	private final String errCode;
	private final String message;
	private final HttpStatus status;

	ErrorCode(String errCode, String message, HttpStatus status) {
		this.status = status;
		this.errCode = errCode;
		this.message = message;
	}

	@Override
	public String toString() {
		return "ErrorCode{"
				+ " status='"
				+ status
				+ '\''
				+ "errCode='"
				+ errCode
				+ '\''
				+ ", message='"
				+ message
				+ '\''
				+ '}';
	}
}
