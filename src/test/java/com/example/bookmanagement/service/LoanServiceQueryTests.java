package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.exception.LoanNotFoundException;
import com.example.bookmanagement.repository.LoanRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoanServiceQueryTests {
    private static final int OWNER_ID = 1;
    private static final int OTHER_ID = 2;
    private static final int LOAN_ID = 42;

    private final LoanRepository loanRepository = mock(LoanRepository.class);
    private final LoanService service = new LoanService(loanRepository, null, null);

    @Test
    void ownerCanGetOwnLoan() {
        Loan loan = loanOwnedBy(OWNER_ID);
        when(loanRepository.getLoanById(LOAN_ID)).thenReturn(loan);

        assertSame(loan, service.getLoanById(LOAN_ID, OWNER_ID, false));
    }

    @Test
    void otherUserGetsNotFoundForSomeoneElsesLoan() {
        when(loanRepository.getLoanById(LOAN_ID)).thenReturn(loanOwnedBy(OWNER_ID));

        assertThrows(LoanNotFoundException.class, () -> service.getLoanById(LOAN_ID, OTHER_ID, false));
    }

    @Test
    void administratorCanGetAnyLoan() {
        Loan loan = loanOwnedBy(OWNER_ID);
        when(loanRepository.getLoanById(LOAN_ID)).thenReturn(loan);

        assertSame(loan, service.getLoanById(LOAN_ID, OTHER_ID, true));
    }

    @Test
    void overdueListUsesTodayInTokyoAsThreshold() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));

        service.getLoanWithBookDetails(true);

        verify(loanRepository).getOverdueLoanWithBookDetail(today);
        verify(loanRepository, never()).getAllLoanWithBookDetail();
    }

    @Test
    void listWithoutOverdueFilterReturnsAllLoans() {
        service.getLoanWithBookDetails(false);

        verify(loanRepository).getAllLoanWithBookDetail();
        verify(loanRepository, never()).getOverdueLoanWithBookDetail(org.mockito.ArgumentMatchers.any());
    }

    private static Loan loanOwnedBy(int userId) {
        Loan loan = new Loan();
        loan.setId(LOAN_ID);
        loan.setBorrowedUser(userId);
        assertEquals(userId, loan.getBorrowedUser());
        return loan;
    }
}
