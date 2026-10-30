package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String mail;
    private String name;
    private String plainPassword;
}
