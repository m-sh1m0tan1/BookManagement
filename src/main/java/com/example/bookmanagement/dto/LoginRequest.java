package com.example.bookmanagement.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String mail;
    private String plainPassword;
}
