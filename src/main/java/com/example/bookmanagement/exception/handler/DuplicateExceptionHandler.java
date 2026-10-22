package com.example.bookmanagement.exception.handler;

import com.example.bookmanagement.dto.DuplicateErrorResponse;
import com.example.bookmanagement.exception.MailDuplicateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DuplicateExceptionHandler {
    @ExceptionHandler({
            MailDuplicateException.class
    })
    public ResponseEntity<DuplicateErrorResponse> handleDuplicateException(RuntimeException e) {
        DuplicateErrorResponse response = new DuplicateErrorResponse();
        if (e instanceof MailDuplicateException) {
            response.setResource("Mail");
            response.setMessage("Mail Duplicate");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

}
