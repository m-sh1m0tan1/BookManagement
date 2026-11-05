package com.example.bookmanagement.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DueDateRequest {
    private LocalDate dueDate;
}
