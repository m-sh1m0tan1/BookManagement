package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.exception.LoanNotFoundException;
import com.example.bookmanagement.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {
    private final LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<Loan> getLoansByBorrowedUserId(int id) {
        List<Loan> loans = loanRepository.getLoansByBorrowedUserId(id);
        if (loans == null) {
            throw new LoanNotFoundException("貸出記録が見つかりません");
        }
        return loans;
    }
}
