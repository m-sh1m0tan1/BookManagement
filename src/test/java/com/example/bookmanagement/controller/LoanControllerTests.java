package com.example.bookmanagement.controller;

import com.example.bookmanagement.auth.CustomUserDetails;
import com.example.bookmanagement.auth.JwtAuthenticationFilter;
import com.example.bookmanagement.auth.JwtService;
import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.LoanNotFoundException;
import com.example.bookmanagement.security.SecurityConfig;
import com.example.bookmanagement.service.LoanService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class LoanControllerTests {
    private static final int USER_ID = 7;
    private static final int LOAN_ID = 42;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/loans/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/loans").contentType("application/json").content("{\"bookId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotListAllLoans() throws Exception {
        mockMvc.perform(get("/api/loans").with(principal(false)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/loans").param("overdue", "true").with(principal(false)))
                .andExpect(status().isForbidden());

        verify(loanService, never()).getLoanWithBookDetails(anyBoolean());
    }

    @Test
    void administratorCanListAllAndOverdueLoans() throws Exception {
        when(loanService.getLoanWithBookDetails(anyBoolean())).thenReturn(List.of());

        mockMvc.perform(get("/api/loans").with(principal(true))).andExpect(status().isOk());
        mockMvc.perform(get("/api/loans").param("overdue", "true").with(principal(true)))
                .andExpect(status().isOk());

        verify(loanService).getLoanWithBookDetails(false);
        verify(loanService).getLoanWithBookDetails(true);
    }

    @Test
    void myLoansUseThePrincipalUserId() throws Exception {
        when(loanService.getLoanWithBookDetailByUserId(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/loans/me").with(principal(false)))
                .andExpect(status().isOk());

        verify(loanService).getLoanWithBookDetailByUserId(USER_ID);
    }

    @Test
    void getLoanPassesPrincipalIdAndAdminFlagToService() throws Exception {
        when(loanService.getLoanById(eq(LOAN_ID), anyInt(), anyBoolean())).thenReturn(loan());

        mockMvc.perform(get("/api/loans/{id}", LOAN_ID).with(principal(false)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/loans/{id}", LOAN_ID).with(principal(true)))
                .andExpect(status().isOk());

        verify(loanService).getLoanById(LOAN_ID, USER_ID, false);
        verify(loanService).getLoanById(LOAN_ID, USER_ID, true);
    }

    @Test
    void getOthersLoanReturnsNotFound() throws Exception {
        when(loanService.getLoanById(LOAN_ID, USER_ID, false))
                .thenThrow(new LoanNotFoundException("貸出記録が見つかりません"));

        mockMvc.perform(get("/api/loans/{id}", LOAN_ID).with(principal(false)))
                .andExpect(status().isNotFound());
    }

    @Test
    void insertLoanUsesPrincipalAsBorrowerAndReturnsCreated() throws Exception {
        when(loanService.insertLoan(any(Loan.class), eq(USER_ID))).thenReturn(loan());

        mockMvc.perform(post("/api/loans")
                        .with(principal(false))
                        .contentType("application/json")
                        .content("{\"bookId\":3,\"borrowedUser\":999}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/loans/" + LOAN_ID));

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanService).insertLoan(captor.capture(), eq(USER_ID));
        assertEquals(USER_ID, captor.getValue().getBorrowedUser());
        assertEquals(3, captor.getValue().getBorrowedBook());
    }

    @Test
    void returnLoanUsesPrincipalUserId() throws Exception {
        when(loanService.updateReturnedAtById(LOAN_ID, USER_ID)).thenReturn(loan());

        mockMvc.perform(delete("/api/loans/{id}", LOAN_ID).with(principal(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LOAN_ID));

        verify(loanService).updateReturnedAtById(LOAN_ID, USER_ID);
    }

    @Test
    void extendLoanPassesDueDateAndPrincipalUserId() throws Exception {
        when(loanService.updateDueDateById(eq(LOAN_ID), any(LocalDate.class), eq(USER_ID))).thenReturn(loan());

        mockMvc.perform(put("/api/loans/{id}/due-date", LOAN_ID)
                        .with(principal(false))
                        .contentType("application/json")
                        .content("{\"dueDate\":\"2026-12-01\"}"))
                .andExpect(status().isOk());

        verify(loanService).updateDueDateById(LOAN_ID, LocalDate.of(2026, 12, 1), USER_ID);
    }

    private static Loan loan() {
        Loan loan = new Loan();
        loan.setId(LOAN_ID);
        loan.setBorrowedUser(USER_ID);
        return loan;
    }

    private RequestPostProcessor principal(boolean isAdmin) {
        User user = new User();
        user.setId(USER_ID);
        user.setMail(isAdmin ? "admin@example.test" : "user@example.test");
        user.setName(isAdmin ? "管理者" : "一般利用者");
        user.setAdmin(isAdmin);
        return user(new CustomUserDetails(user));
    }
}
