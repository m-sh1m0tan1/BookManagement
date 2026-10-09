package com.example.bookmanagement.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowBook {
    private int id;
    private int borrowedUser;
    private int borrowedBook;
    private LocalDateTime borrowedAt;
    private LocalDate dueDate;
    private LocalDateTime returnedAt;
}
