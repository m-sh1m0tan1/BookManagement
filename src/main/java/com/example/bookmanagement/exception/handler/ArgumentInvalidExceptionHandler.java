package com.example.bookmanagement.exception.handler;

import com.example.bookmanagement.dto.ArgumentErrorResponse;
import com.example.bookmanagement.exception.BookArgumentInvalidException;
import com.example.bookmanagement.exception.LoanArgumentInvalidException;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ArgumentInvalidExceptionHandler {
    @ExceptionHandler({
            UserArgumentInvalidException.class,
            BookArgumentInvalidException.class,
            LoanArgumentInvalidException.class,
    })
    public ResponseEntity<ArgumentErrorResponse> handleArgumentInvalidException(RuntimeException e) {
        ArgumentErrorResponse response = new ArgumentErrorResponse();
        if (e instanceof UserArgumentInvalidException) {
            response.setResource("User Argument Invalid");
            response.setMessage(e.getMessage());
        } else if (e instanceof BookArgumentInvalidException) {
            response.setResource("Book Argument Invalid");
            response.setMessage(e.getMessage());
        } else if (e instanceof LoanArgumentInvalidException) {
            response.setResource("Loan Argument Invalid");
            response.setMessage(e.getMessage());
        }
        return ResponseEntity.badRequest().body(response);
    }
}
