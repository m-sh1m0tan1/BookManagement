package com.example.bookmanagement.controller;

import com.example.bookmanagement.auth.AuthenticationService;
import com.example.bookmanagement.auth.JwtAuthenticationFilter;
import com.example.bookmanagement.auth.JwtService;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.handler.ArgumentInvalidExceptionHandler;
import com.example.bookmanagement.exception.handler.DuplicateExceptionHandler;
import com.example.bookmanagement.repository.UserRepository;
import com.example.bookmanagement.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    AuthenticationService.class,
    ArgumentInvalidExceptionHandler.class,
    DuplicateExceptionHandler.class
})
class AuthControllerRegistrationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void emptyMailReturns400() throws Exception {
        mockMvc.perform(register("", "Reader", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.resource").value("User Argument Invalid"));
        verify(userRepository, never()).insertUser(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "abc@", "@example.test", "a@b", "a b@example.test", " a@example.test", "a@@example.test"})
    void malformedMailReturns400(String mail) throws Exception {
        mockMvc.perform(register(mail, "Reader", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resource").value("User Argument Invalid"));
        verify(userRepository, never()).insertUser(any());
    }

    @Test
    void emptyPasswordReturns400() throws Exception {
        mockMvc.perform(register("reader@example.test", "Reader", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.resource").value("User Argument Invalid"));
        verify(userRepository, never()).insertUser(any());
    }

    @Test
    void blankNameReturns400() throws Exception {
        mockMvc.perform(register("reader@example.test", " ", "password"))
                .andExpect(status().isBadRequest());
        verify(userRepository, never()).insertUser(any());
    }

    @Test
    void existingMailReturns409() throws Exception {
        when(userRepository.getUserByMail("reader@example.test")).thenReturn(new User());

        mockMvc.perform(register("reader@example.test", "Reader", "password"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.resource").value("Mail"));
        verify(userRepository, never()).insertUser(any());
    }

    @Test
    void insertConflictAfterPrecheckReturns409() throws Exception {
        when(userRepository.getUserByMail("reader@example.test")).thenReturn(null);
        when(userRepository.insertUser(any())).thenReturn(0);

        mockMvc.perform(register("reader@example.test", "Reader", "password"))
                .andExpect(status().isConflict());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void validRegistrationReturns201() throws Exception {
        when(userRepository.getUserByMail("reader@example.test")).thenReturn(null);
        when(userRepository.insertUser(any())).thenReturn(1);
        when(jwtService.generateToken("reader@example.test")).thenReturn("token");

        mockMvc.perform(register("reader@example.test", "Reader", "password"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder register(
            String mail, String name, String password) {
        return post("/api/auth/register")
                .contentType("application/json")
                .content("{\"mail\":\"" + mail + "\",\"name\":\"" + name + "\",\"plainPassword\":\"" + password + "\"}");
    }
}
