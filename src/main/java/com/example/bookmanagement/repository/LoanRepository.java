package com.example.bookmanagement.repository;

import com.example.bookmanagement.dto.LoanWithBookDetail;
import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.mapper.LoanMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class LoanRepository {
    private final LoanMapper loanMapper;

    public LoanRepository(LoanMapper loanMapper) {
        this.loanMapper = loanMapper;
    }

    public List<Loan> getLoansByBorrowedUserId(int id) {
        return loanMapper.getLoansByBorrowedUserId(id);
    }

    public Loan getLoanById(int id) {
        return loanMapper.getLoanById(id);
    }

    public Loan getLoanByIdForUpdate(int id) {
        return loanMapper.getLoanByIdForUpdate(id);
    }

    public List<Loan> getUnReturnedLoanByUserId(int id) {
        return loanMapper.getUnReturnedLoanByUserId(id);
    }

    public List<Loan> getUnReturnedLoanByBookId(int id) {
        return loanMapper.getUnReturnedLoanByBookId(id);
    }

    public List<LoanWithBookDetail> getLoanWithBookDetailByUserId(int userId) {
        return loanMapper.getLoanWithBookDetailByUserId(userId);
    }

    public int insertLoan(Loan loan) {
        return loanMapper.insertLoan(loan);
    }

    public Loan updateReturnedAtById(int id) {
        if (loanMapper.updateReturnedAtById(id) == 1) {
            return getLoanById(id);
        }
        return null;
    }

    public Loan updateDueDateById(int id, LocalDate dueDate) {
        if (loanMapper.updateDueDateById(id, dueDate) == 1) {
            return getLoanById(id);
        }
        return null;
    }
}
