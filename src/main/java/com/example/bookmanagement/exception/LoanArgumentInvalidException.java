package com.example.bookmanagement.exception;

public class LoanArgumentInvalidException extends RuntimeException {
    public LoanArgumentInvalidException(String message) {
        super(message);
    }
}
