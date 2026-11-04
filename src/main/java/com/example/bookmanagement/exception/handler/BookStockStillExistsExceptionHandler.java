package com.example.bookmanagement.exception.handler;

import com.example.bookmanagement.dto.DuplicateErrorResponse;
import com.example.bookmanagement.exception.BookStockStillExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BookStockStillExistsExceptionHandler {

    @ExceptionHandler(BookStockStillExistsException.class)
    public ResponseEntity<DuplicateErrorResponse> handleBookStockStillExistsException(BookStockStillExistsException e) {
        DuplicateErrorResponse response = new DuplicateErrorResponse();
        response.setResource("Book");
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
