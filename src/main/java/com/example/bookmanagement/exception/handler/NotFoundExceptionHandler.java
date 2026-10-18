package com.example.bookmanagement.exception.handler;

import com.example.bookmanagement.exception.BookNotFoundException;
import com.example.bookmanagement.exception.LoanNotFoundException;
import com.example.bookmanagement.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotFoundExceptionHandler {
    @ExceptionHandler({
            UserNotFoundException.class,
            BookNotFoundException.class,
            LoanNotFoundException.class
    })
    public ResponseEntity<String> handleNotFoundException(RuntimeException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}
