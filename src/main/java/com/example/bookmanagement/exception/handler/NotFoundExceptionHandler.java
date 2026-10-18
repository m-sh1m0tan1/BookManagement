package com.example.bookmanagement.exception.handler;

import com.example.bookmanagement.dto.NotFoundErrorResponse;
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
    public ResponseEntity<NotFoundErrorResponse> handleNotFoundException(RuntimeException e){

        NotFoundErrorResponse response = new NotFoundErrorResponse();
        if (e instanceof UserNotFoundException) {
            response.setResource("user");
            response.setMessage("User Not Found");
        } else if (e instanceof BookNotFoundException) {
            response.setResource("book");
            response.setMessage("Book Not Found");
        } else if (e instanceof LoanNotFoundException) {
            response.setResource("loan");
            response.setMessage("Loan Not Found");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
