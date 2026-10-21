package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.LoanArgumentInvalidException;
import com.example.bookmanagement.repository.BookRepository;
import com.example.bookmanagement.repository.LoanRepository;
import com.example.bookmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LoanConcurrencyTests {
    @Autowired
    private LoanService loanService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<Integer> userIds = new ArrayList<>();
    private final List<Integer> bookIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (int userId : userIds) {
            jdbcTemplate.update("delete from loans where borrowed_user = ?", userId);
        }
        for (int bookId : bookIds) {
            jdbcTemplate.update("delete from loans where borrowed_book = ?", bookId);
            jdbcTemplate.update("delete from books where id = ?", bookId);
        }
        for (int userId : userIds) {
            jdbcTemplate.update("delete from users where id = ?", userId);
        }
        userIds.clear();
        bookIds.clear();
    }

    @Test
    void concurrentBorrowersCannotExceedBookQuantity() throws Exception {
        User firstUser = createUser();
        User secondUser = createUser();
        Book book = createBook(1);

        int successfulLoans = runConcurrently(
                firstUser.getId(), book.getId(), secondUser.getId(), book.getId());

        assertEquals(1, successfulLoans);
        assertEquals(1, loanRepository.getUnReturnedLoanByBookId(book.getId()).size());
    }

    @Test
    void concurrentRequestsCannotExceedUsersThreeLoanLimit() throws Exception {
        User user = createUser();
        Book firstExistingBook = createBook(1);
        Book secondExistingBook = createBook(1);
        loanService.insertLoan(createLoan(user.getId(), firstExistingBook.getId()));
        loanService.insertLoan(createLoan(user.getId(), secondExistingBook.getId()));

        Book firstRequestedBook = createBook(1);
        Book secondRequestedBook = createBook(1);
        int successfulLoans = runConcurrently(
                user.getId(), firstRequestedBook.getId(), user.getId(), secondRequestedBook.getId());

        assertEquals(1, successfulLoans);
        assertEquals(3, loanRepository.getUnReturnedLoanByUserId(user.getId()).size());
    }

    private int runConcurrently(int firstUserId, int firstBookId, int secondUserId, int secondBookId)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = executor.submit(concurrentBorrow(firstUserId, firstBookId, ready, start));
            Future<Boolean> second = executor.submit(concurrentBorrow(secondUserId, secondBookId, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS), "貸出リクエストが開始待ちになりませんでした");
            start.countDown();
            return (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> concurrentBorrow(int userId, int bookId, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("貸出リクエストの開始待ちがタイムアウトしました");
            }
            try {
                loanService.insertLoan(createLoan(userId, bookId));
                return true;
            } catch (LoanArgumentInvalidException e) {
                return false;
            }
        };
    }

    private User createUser() {
        String token = UUID.randomUUID().toString();
        User user = new User();
        user.setMail("loan-concurrency-" + token + "@example.test");
        user.setName("同時貸出テスト");
        user.setPassword(passwordEncoder.encode(token));
        assertEquals(1, userRepository.insertUser(user));
        userIds.add(user.getId());
        return user;
    }

    private Book createBook(int quantity) {
        Book book = new Book();
        book.setName("同時貸出テスト-" + UUID.randomUUID());
        book.setAuthor("テスト著者");
        book.setQuantity(quantity);
        assertEquals(1, bookRepository.insertBook(book));
        bookIds.add(book.getId());
        return book;
    }

    private Loan createLoan(int userId, int bookId) {
        Loan loan = new Loan();
        loan.setBorrowedUser(userId);
        loan.setBorrowedBook(bookId);
        return loan;
    }
}
