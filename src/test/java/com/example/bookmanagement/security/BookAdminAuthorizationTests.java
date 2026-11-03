package com.example.bookmanagement.security;

import com.example.bookmanagement.auth.CustomUserDetails;
import com.example.bookmanagement.auth.JwtAuthenticationFilter;
import com.example.bookmanagement.auth.JwtService;
import com.example.bookmanagement.controller.BookController;
import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.form.BookForm;
import com.example.bookmanagement.security.SecurityConfig;
import com.example.bookmanagement.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class BookAdminAuthorizationTests {
    private static final int BOOK_ID = 42;
    private static final String BOOK_JSON = "{\"name\":\"Test Book\",\"author\":\"Test Author\",\"quantity\":1}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void regularUserCannotCreateUpdateOrDeleteBooks() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(principal(false))
                        .contentType("application/json")
                        .content(BOOK_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/books/{id}", BOOK_ID)
                        .with(principal(false))
                        .contentType("application/json")
                        .content(BOOK_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/books/{id}", BOOK_ID)
                        .with(principal(false)))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateUpdateAndDeleteBooks() throws Exception {
        Book book = new Book();
        book.setId(BOOK_ID);
        when(bookService.insertBook(any(BookForm.class))).thenReturn(book);
        when(bookService.updateBook(any(BookForm.class), eq(BOOK_ID))).thenReturn(book);
        when(bookService.softDeleteBookById(BOOK_ID)).thenReturn(true);

        mockMvc.perform(post("/api/books")
                        .with(principal(true))
                        .contentType("application/json")
                        .content(BOOK_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/books/{id}", BOOK_ID)
                        .with(principal(true))
                        .contentType("application/json")
                        .content(BOOK_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/books/{id}", BOOK_ID)
                        .with(principal(true)))
                .andExpect(status().isNoContent());
    }

    private RequestPostProcessor principal(boolean isAdmin) {
        User user = new User();
        user.setMail(isAdmin ? "admin@example.test" : "user@example.test");
        user.setName(isAdmin ? "管理者" : "一般利用者");
        user.setAdmin(isAdmin);
        return user(new CustomUserDetails(user));
    }
}
