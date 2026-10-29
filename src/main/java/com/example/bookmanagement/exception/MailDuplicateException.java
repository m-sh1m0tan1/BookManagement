package com.example.bookmanagement.exception;

public class MailDuplicateException extends RuntimeException {
    public MailDuplicateException(String message) {
        super(message);
    }
}
