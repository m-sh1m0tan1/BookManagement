package com.example.bookmanagement.entity;

import lombok.Data;
import lombok.ToString;

@Data
public class User {
    private int id;
    private String mail;
    private String name;
    @ToString.Exclude
    private String password;
    private boolean isAdmin;
}
