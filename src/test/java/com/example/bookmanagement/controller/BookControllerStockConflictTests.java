package com.example.bookmanagement.controller;

import com.example.bookmanagement.auth.CustomUserDetails;
import com.example.bookmanagement.auth.JwtAuthenticationFilter;
import com.example.bookmanagement.auth.JwtService;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.BookStockStillExistsException;
import com.example.bookmanagement.security.SecurityConfig;
import com.example.bookmanagement.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class BookControllerStockConflictTests {
    private static final int BOOK_ID = 42;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void deletingBookWithStockReturns409AsJson() throws Exception {
        when(bookService.softDeleteBookById(BOOK_ID))
                .thenThrow(new BookStockStillExistsException("削除しようとしている本の在庫がまだあります"));

        mockMvc.perform(delete("/api/books/{id}", BOOK_ID).with(admin()))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.resource").value("Book"))
                .andExpect(jsonPath("$.message").value("削除しようとしている本の在庫がまだあります"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        User user = new User();
        user.setMail("admin@example.test");
        user.setName("管理者");
        user.setAdmin(true);
        return user(new CustomUserDetails(user));
    }
}
