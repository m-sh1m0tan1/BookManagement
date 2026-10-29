package com.example.bookmanagement.service;

import com.example.bookmanagement.dto.LoanWithBookDetail;
import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.BookNotFoundException;
import com.example.bookmanagement.exception.LoanArgumentInvalidException;
import com.example.bookmanagement.exception.LoanNotFoundException;
import com.example.bookmanagement.exception.UserNotFoundException;
import com.example.bookmanagement.repository.BookRepository;
import com.example.bookmanagement.repository.LoanRepository;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<Loan> getLoansByBorrowedUserId(int id) {
        List<Loan> loans = loanRepository.getLoansByBorrowedUserId(id);
        if (loans == null) {
            throw new LoanNotFoundException("貸出記録が見つかりません");
        }
        return loans;
    }

    public Loan getLoanById(int id) {
        Loan loan = loanRepository.getLoanById(id);
        if (loan == null) {
            throw new LoanNotFoundException("貸出記録が見つかりません");
        }
        return loan;
    }

    public List<Loan> getUnReturnedLoanByUserId(int id) {
        if (userRepository.getUserById(id) == null) {
            throw new UserNotFoundException("ユーザが見つかりません");
        }
        List<Loan> loans = loanRepository.getUnReturnedLoanByUserId(id);
        if (loans == null) {
            throw new LoanNotFoundException("貸出記録が見つからないか、あなたが全て返却しています");
        }
        return loans;
    }

    public List<Loan> getUnReturnedLoanByBookId(int id) {
        if (bookRepository.getBookById(id) == null) {
            throw new BookNotFoundException("本が見つかりません");
        }
        List<Loan> loans = loanRepository.getUnReturnedLoanByBookId(id);
        if (loans == null) {
            throw new LoanNotFoundException("貸出記録が見つからないか、この本は全て返却されています");
        }
        return loans;
    }

    public List<LoanWithBookDetail> getLoanWithBookDetailByUserId(int id) {
        if (userRepository.getUserById(id) == null) {
            throw new UserNotFoundException("ユーザが見つかりません");
        }
        List<LoanWithBookDetail> loans = loanRepository.getLoanWithBookDetailByUserId(id);
        if (loans == null) {
            throw new LoanNotFoundException("貸出記録が見つかりません");
        }
        return loans;
    }

    @Transactional
    public Loan insertLoan(Loan loan, int userId) {
        if (loan == null) {
            throw new LoanArgumentInvalidException("貸出記録が指定されていません");
        }

        if (loan.getBorrowedUser() != userId) {
            throw new LoanArgumentInvalidException("不正なリクエストが送信されました");
        }

        User loanIssuedUser = userRepository.getUserByIdForUpdate(loan.getBorrowedUser());
        if (loanIssuedUser == null) {
            throw new UserNotFoundException("ユーザが見つかりません");
        }


        Book targetBook = bookRepository.getBookByIdForUpdate(loan.getBorrowedBook());
        if (targetBook == null) {
            throw new BookNotFoundException("本が見つかりません");
        }
        List<Loan> borrowedLoans = loanRepository.getUnReturnedLoanByUserId(loan.getBorrowedUser());
        if (borrowedLoans.size() >= 3){
            throw new LoanArgumentInvalidException("貸し出せる本の上限に達しました");
        }

        List<Loan> unreturnedLoans = loanRepository.getUnReturnedLoanByBookId(loan.getBorrowedBook());
        if (unreturnedLoans.size() >= targetBook.getQuantity()) {
            throw new LoanArgumentInvalidException("貸し出せる本の上限に達しました");
        }
        loan.setDueDate(LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(14));
        if (loanRepository.insertLoan(loan) != 1) {
            throw new RuntimeException("貸出記録の登録に失敗しました");
        }
        return getLoanById(loan.getId());
    }

    @Transactional
    public Loan updateReturnedAtById(int loanId, int userId) {
        Loan loan = loanRepository.getLoanByIdForUpdate(loanId);
        boolean isLoanIssuedUserMatched = isLoanIssuedUserMatched(loanId, userId);
        if (!isLoanIssuedUserMatched) {
            throw new LoanArgumentInvalidException("不正なリクエストが送信されました");
        }

        if (loan.getReturnedAt() != null) {
            throw new LoanArgumentInvalidException("貸出記録が返却済みです");
        }
        return loanRepository.updateReturnedAtById(loanId);
    }

    @Transactional
    public Loan updateDueDateById(int loanId, LocalDate dueDate, int userId) {
        Loan loan = loanRepository.getLoanByIdForUpdate(loanId);
        boolean isLoanIssuedUserMatched = isLoanIssuedUserMatched(loanId, userId);
        if (!isLoanIssuedUserMatched) {
            throw new LoanArgumentInvalidException("不正なリクエストが送信されました");
        }
        if (loan.getReturnedAt() != null) {
            throw new LoanArgumentInvalidException("貸出記録が返却済みです");
        }
        if (loan.isExtended()) {
            throw new LoanArgumentInvalidException("貸出記録が既に延長済みです");
        }
        if (LocalDate.now(ZoneId.of("Asia/Tokyo")).isAfter(loan.getDueDate())) {
            throw new LoanArgumentInvalidException("返却期限を過ぎた貸し出しは延長できません");
        }
        if (dueDate == null || !dueDate.isAfter(loan.getDueDate())) {
            throw new LoanArgumentInvalidException("新しい返却期限は現在の返却期限より後にしてください");
        }
        return loanRepository.updateDueDateById(loanId, dueDate);
    }

    private boolean isLoanIssuedUserMatched(int loanId, int userId) {
        Loan loan = loanRepository.getLoanByIdForUpdate(loanId);
        User user = userRepository.getUserByIdForUpdate(userId);
        if (loan == null) {
            throw new LoanNotFoundException("貸出記録が見つかりません");
        } else if (user == null) {
            throw new UserNotFoundException("ユーザが見つかりません");
        }
        return loan.getBorrowedUser() == userId;
    }

}
