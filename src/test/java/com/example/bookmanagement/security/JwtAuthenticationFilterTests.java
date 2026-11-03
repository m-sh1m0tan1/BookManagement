package com.example.bookmanagement.security;

import com.example.bookmanagement.auth.JwtAuthenticationFilter;
import com.example.bookmanagement.auth.JwtService;
import com.example.bookmanagement.controller.BookController;
import com.example.bookmanagement.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class JwtAuthenticationFilterTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void tokenForNonexistentUserReturns401() throws Exception {
        when(jwtService.getMailFromToken("old-token")).thenReturn("old@example.test");
        when(userDetailsService.loadUserByUsername("old@example.test"))
                .thenThrow(new UsernameNotFoundException("not found"));

        mockMvc.perform(get("/api/books").header("Authorization", "Bearer old-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenReturns401() throws Exception {
        when(jwtService.getMailFromToken("bad-token")).thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(get("/api/books").header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }
}
