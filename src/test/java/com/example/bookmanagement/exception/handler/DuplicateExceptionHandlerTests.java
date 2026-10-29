package com.example.bookmanagement.exception.handler;

import com.example.bookmanagement.exception.MailDuplicateException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateExceptionHandlerTests {
    @Test
    void mailDuplicateReturnsConflict() {
        DuplicateExceptionHandler handler = new DuplicateExceptionHandler();

        var response = handler.handleDuplicateException(
                new MailDuplicateException("メールアドレスが既に登録されています"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Mail", response.getBody().getResource());
        assertEquals("Mail Duplicate", response.getBody().getMessage());
    }
}
