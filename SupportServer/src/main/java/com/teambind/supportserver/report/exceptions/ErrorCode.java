package com.teambind.supportserver.report.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	REPORT_NOT_FOUND("REPORT_NOT_FOUND", "Report Not Found", HttpStatus.NOT_FOUND),
	REPORT_CATEGORY_NOT_FOUND("REPORT_CATEGORY_NOT_FOUND", "Report Category Not Found", HttpStatus.NOT_FOUND),
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

