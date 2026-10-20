package com.example.bookmanagement.form;

import lombok.Data;

@Data
public class BookForm {
    private String name;
    private String author;
    private String isbn;
    private int quantity;
}
