package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class ArgumentErrorResponse {
    private String resource;
    private String message;
}
