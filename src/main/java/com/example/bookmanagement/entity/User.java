package com.example.bookmanagement.entity;

import lombok.Data;

@Data
public class User {
    private int id;
    private String mail;
    private String name;
    private String password;
    private boolean isAdmin;
}
