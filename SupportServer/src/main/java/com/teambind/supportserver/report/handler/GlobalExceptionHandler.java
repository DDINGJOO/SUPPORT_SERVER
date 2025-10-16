package com.teambind.supportserver.report.handler;

import com.teambind.supportserver.report.exceptions.ReportException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ReportException.class)
	public ResponseEntity<?> handleCustomException(ReportException ex) {
		
		return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
	}
}
