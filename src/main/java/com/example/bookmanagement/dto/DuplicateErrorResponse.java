package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class DuplicateErrorResponse {
    private String resource;
    private String message;
}
