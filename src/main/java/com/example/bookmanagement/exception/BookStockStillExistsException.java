package com.example.bookmanagement.exception;

public class BookStockStillExistsException extends RuntimeException {
    public BookStockStillExistsException(String message) {
        super(message);
    }
}
