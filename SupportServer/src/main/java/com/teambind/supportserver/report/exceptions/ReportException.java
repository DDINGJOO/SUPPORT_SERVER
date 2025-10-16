package com.teambind.supportserver.report.exceptions;

import org.springframework.http.HttpStatus;

public class ReportException extends RuntimeException {
	private final ErrorCode errorcode;
	
	public ReportException(ErrorCode errorcode) {
		
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
