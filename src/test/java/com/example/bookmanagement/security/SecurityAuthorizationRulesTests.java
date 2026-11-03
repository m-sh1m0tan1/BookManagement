package com.example.bookmanagement.security;

import com.example.bookmanagement.auth.CustomUserDetails;
import com.example.bookmanagement.auth.JwtAuthenticationFilter;
import com.example.bookmanagement.auth.JwtService;
import com.example.bookmanagement.controller.BookController;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityAuthorizationRulesTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void regularUserCanReadBooks() throws Exception {
        mockMvc.perform(get("/api/books").with(principal(false)))
                .andExpect(status().isOk());
    }

    @Test
    void regularUserCannotUseAnyOtherMethodOnBooks() throws Exception {
        mockMvc.perform(patch("/api/books/{id}", 42).with(principal(false)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/books/{id}/restore", 42).with(principal(false)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCanOpenStaticPages() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isNotFound());
        mockMvc.perform(get("/login.html")).andExpect(status().isNotFound());
    }

    private RequestPostProcessor principal(boolean isAdmin) {
        User user = new User();
        user.setMail("user@example.test");
        user.setName("利用者");
        user.setAdmin(isAdmin);
        return user(new CustomUserDetails(user));
    }
}
