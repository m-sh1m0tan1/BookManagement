package com.example.bookmanagement.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class LoanWithBookDetail {
    private int id;
    private int userId; // ユーザID
    private String userName; // ユーザ名
    private int bookId; // 本ID
    private String bookName; // 本の名前
    private String bookAuthor; // 本の著者名
    private boolean bookBorrowable; // 本が借りられるか
    private boolean bookDeleted; // 本が論理削除済か
    private OffsetDateTime borrowedAt; // 借りた日時
    private LocalDate dueDate; // 返却期限
    private OffsetDateTime returnedAt; // 返却日時
}
