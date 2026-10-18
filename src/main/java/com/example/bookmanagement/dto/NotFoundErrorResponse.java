package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class NotFoundErrorResponse {
    private String resource;
    private String message;
}
