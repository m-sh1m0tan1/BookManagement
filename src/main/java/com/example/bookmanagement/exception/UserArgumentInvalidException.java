package com.example.bookmanagement.exception;

public class UserArgumentInvalidException extends RuntimeException{
    public UserArgumentInvalidException(String message) {
        super(message);
    }
}
