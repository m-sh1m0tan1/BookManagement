package com.example.bookmanagement.exception;

public class BookArgumentInvalidException extends RuntimeException {
    public BookArgumentInvalidException(String message) {
        super(message);
    }
}
