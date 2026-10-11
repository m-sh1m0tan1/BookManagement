package com.example.bookmanagement.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class Loan {
    private int id;
    private int borrowedUser;
    private int borrowedBook;
    private OffsetDateTime borrowedAt;
    private LocalDate dueDate;
    private OffsetDateTime returnedAt;
}
