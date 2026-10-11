package com.example.bookmanagement.entity;

import lombok.Data;

@Data
public class Book {
    private int id;
    private String name;
    private String author;
    private String isbn;
    private int quantity;
}
