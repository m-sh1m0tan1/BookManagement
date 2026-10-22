package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.LoanArgumentInvalidException;
import com.example.bookmanagement.exception.LoanNotFoundException;
import com.example.bookmanagement.repository.BookRepository;
import com.example.bookmanagement.repository.LoanRepository;
import com.example.bookmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanServiceTests {
    private static final int USER_ID = 1;

    @Test
    void updateDueDateAcceptsLoanDueToday() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        Loan loan = new Loan();
        loan.setId(42);
        loan.setDueDate(today);
        LoanService service = new LoanService(createExtensionRepository(loan), createUserRepository(USER_ID), null);

        Loan updatedLoan = service.updateDueDateById(42, today.plusDays(1), USER_ID);

        assertEquals(today.plusDays(1), updatedLoan.getDueDate());
        assertTrue(updatedLoan.isExtended());
    }

    @Test
    void updateDueDateRejectsLoanDueYesterdayWithoutUpdatingIt() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        Loan loan = new Loan();
        loan.setId(42);
        loan.setDueDate(today.minusDays(1));
        LoanService service = new LoanService(createExtensionRepository(loan), createUserRepository(USER_ID), null);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.updateDueDateById(42, today.plusDays(1), USER_ID));
        assertEquals(today.minusDays(1), loan.getDueDate());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {-1, 0})
    void updateDueDateRejectsMissingOrNonIncreasingDate(Integer dayOffset) {
        LocalDate currentDueDate = LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(7);
        Loan loan = new Loan();
        loan.setId(42);
        loan.setDueDate(currentDueDate);
        LoanService service = new LoanService(createExtensionRepository(loan), createUserRepository(USER_ID), null);
        LocalDate requestedDueDate = dayOffset == null ? null : currentDueDate.plusDays(dayOffset);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.updateDueDateById(42, requestedDueDate, USER_ID));
        assertEquals(currentDueDate, loan.getDueDate());
    }

    @Test
    void updateDueDateAcceptsDateAfterCurrentDueDate() {
        Loan loan = new Loan();
        loan.setId(42);
        loan.setDueDate(LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(7));
        LocalDate requestedDueDate = loan.getDueDate().plusDays(1);
        LoanService service = new LoanService(createExtensionRepository(loan), createUserRepository(USER_ID), null);

        Loan updatedLoan = service.updateDueDateById(42, requestedDueDate, USER_ID);

        assertEquals(requestedDueDate, updatedLoan.getDueDate());
    }

    @Test
    void updateDueDateRejectsReturnedLoanWithoutUpdatingIt() {
        Loan returnedLoan = new Loan();
        returnedLoan.setId(42);
        returnedLoan.setBorrowedUser(USER_ID);
        returnedLoan.setDueDate(LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(7));
        returnedLoan.setReturnedAt(OffsetDateTime.parse("2026-01-01T09:00:00+09:00"));
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public Loan getLoanByIdForUpdate(int id) {
                return returnedLoan;
            }

            @Override
            public Loan updateDueDateById(int id, LocalDate dueDate) {
                throw new AssertionError("Returned loan must not be updated");
            }
        };
        LoanService service = new LoanService(repository, createUserRepository(USER_ID), null);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.updateDueDateById(42, returnedLoan.getDueDate().plusDays(1), USER_ID));
    }

    @Test
    void updateDueDateRejectsAlreadyExtendedLoanWithoutUpdatingIt() {
        LocalDate currentDueDate = LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(7);
        Loan loan = new Loan();
        loan.setId(42);
        loan.setDueDate(currentDueDate);
        loan.setExtended(true);
        LoanService service = new LoanService(createExtensionRepository(loan), createUserRepository(USER_ID), null);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.updateDueDateById(42, currentDueDate.plusDays(1), USER_ID));
        assertEquals(currentDueDate, loan.getDueDate());
    }

    @Test
    void updateReturnedAtRejectsAlreadyReturnedLoanWithoutUpdatingIt() {
        OffsetDateTime returnedAt = OffsetDateTime.parse("2026-01-01T09:00:00+09:00");
        Loan returnedLoan = new Loan();
        returnedLoan.setId(42);
        returnedLoan.setBorrowedUser(USER_ID);
        returnedLoan.setReturnedAt(returnedAt);
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public Loan getLoanByIdForUpdate(int id) {
                return returnedLoan;
            }

            @Override
            public Loan updateReturnedAtById(int id) {
                throw new AssertionError("Returned loan must not be updated");
            }
        };
        LoanService service = new LoanService(repository, createUserRepository(USER_ID), null);

        assertThrows(LoanArgumentInvalidException.class, () -> service.updateReturnedAtById(42, USER_ID));
        assertEquals(returnedAt, returnedLoan.getReturnedAt());
    }

    @Test
    void updateReturnedAtRejectsAnotherUsersLoanWithoutUpdatingIt() {
        Loan loan = new Loan();
        loan.setId(42);
        loan.setBorrowedUser(2);
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public Loan getLoanByIdForUpdate(int id) {
                return loan;
            }

            @Override
            public Loan updateReturnedAtById(int id) {
                throw new AssertionError("Another user's loan must not be updated");
            }
        };
        LoanService service = new LoanService(repository, createUserRepository(USER_ID), null);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.updateReturnedAtById(42, USER_ID));
    }

    @Test
    void updateDueDateRejectsAnotherUsersLoanWithoutUpdatingIt() {
        Loan loan = new Loan();
        loan.setId(42);
        loan.setBorrowedUser(2);
        loan.setDueDate(LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(7));
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public Loan getLoanByIdForUpdate(int id) {
                return loan;
            }

            @Override
            public Loan updateDueDateById(int id, LocalDate dueDate) {
                throw new AssertionError("Another user's loan must not be updated");
            }
        };
        LoanService service = new LoanService(repository, createUserRepository(USER_ID), null);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.updateDueDateById(42, loan.getDueDate().plusDays(1), USER_ID));
    }

    @Test
    void updateReturnedAtReportsMissingLoan() {
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public Loan getLoanByIdForUpdate(int id) {
                return null;
            }
        };
        LoanService service = new LoanService(repository, createUserRepository(USER_ID), null);

        assertThrows(LoanNotFoundException.class,
                () -> service.updateReturnedAtById(42, USER_ID));
    }

    @Test
    void insertLoanRejectsNullInput() {
        LoanService service = new LoanService(null, null, null);

        assertThrows(LoanArgumentInvalidException.class, () -> service.insertLoan(null, USER_ID));
    }

    @Test
    void insertLoanRejectsMismatchedBorrowerBeforeLockingRequestUser() {
        List<Integer> lockedUserIds = new ArrayList<>();
        UserRepository userRepository = new UserRepository(null) {
            @Override
            public User getUserByIdForUpdate(int id) {
                lockedUserIds.add(id);
                User user = new User();
                user.setId(id);
                return user;
            }
        };
        Loan input = new Loan();
        input.setBorrowedUser(2);
        LoanService service = new LoanService(null, userRepository, null);

        assertThrows(LoanArgumentInvalidException.class,
                () -> service.insertLoan(input, USER_ID));
        assertTrue(lockedUserIds.isEmpty());
    }

    @Test
    void insertLoanOverridesRequestedDueDateWithFourteenDaysFromTokyoToday() {
        Loan[] savedLoan = new Loan[1];
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public List<Loan> getUnReturnedLoanByUserId(int id) {
                return List.of();
            }

            @Override
            public List<Loan> getUnReturnedLoanByBookId(int id) {
                return List.of();
            }

            @Override
            public int insertLoan(Loan loan) {
                loan.setId(42);
                savedLoan[0] = loan;
                return 1;
            }

            @Override
            public Loan getLoanById(int id) {
                return id == 42 ? savedLoan[0] : null;
            }
        };
        Loan input = new Loan();
        input.setBorrowedUser(1);
        input.setBorrowedBook(2);
        input.setDueDate(LocalDate.of(2000, 1, 1));
        LocalDate earliestDueDate = LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(14);

        Loan createdLoan = createAvailableLoanService(repository).insertLoan(input, USER_ID);

        LocalDate latestDueDate = LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(14);
        assertTrue(!createdLoan.getDueDate().isBefore(earliestDueDate)
                && !createdLoan.getDueDate().isAfter(latestDueDate));
    }

    @Test
    void insertLoanReturnsStoredBorrowedAt() {
        OffsetDateTime borrowedAt = OffsetDateTime.parse("2026-01-01T09:00:00+09:00");
        Loan storedLoan = new Loan();
        storedLoan.setId(42);
        storedLoan.setBorrowedUser(1);
        storedLoan.setBorrowedBook(2);
        storedLoan.setBorrowedAt(borrowedAt);
        storedLoan.setDueDate(LocalDate.of(2026, 1, 15));
        LoanRepository repository = new LoanRepository(null) {
            @Override
            public List<Loan> getUnReturnedLoanByUserId(int id) {
                return List.of();
            }

            @Override
            public List<Loan> getUnReturnedLoanByBookId(int id) {
                return List.of();
            }

            @Override
            public int insertLoan(Loan loan) {
                loan.setId(42);
                return 1;
            }

            @Override
            public Loan getLoanById(int id) {
                return id == 42 ? storedLoan : null;
            }
        };
        Loan input = new Loan();
        input.setBorrowedUser(1);
        input.setBorrowedBook(2);
        input.setDueDate(LocalDate.of(2026, 1, 15));

        Loan createdLoan = createAvailableLoanService(repository).insertLoan(input, USER_ID);

        assertEquals(42, createdLoan.getId());
        assertEquals(borrowedAt, createdLoan.getBorrowedAt());
    }

    @Test
    void insertLoanRejectsWhenUserAlreadyHasThreeUnreturnedLoans() {
        List<String> calls = new ArrayList<>();
        Book book = new Book();
        book.setQuantity(10);
        LoanService service = createLoanService(createLoans(3), book, List.of(), calls);
        Loan input = new Loan();
        input.setBorrowedUser(1);
        input.setBorrowedBook(2);

        assertThrows(LoanArgumentInvalidException.class, () -> service.insertLoan(input, USER_ID));
        assertEquals(List.of("lock-user", "lock-book", "count-user"), calls);
    }

    @Test
    void insertLoanRejectsWhenNoBookCopyIsAvailable() {
        List<String> calls = new ArrayList<>();
        Book book = new Book();
        book.setQuantity(1);
        LoanService service = createLoanService(List.of(), book, createLoans(1), calls);
        Loan input = new Loan();
        input.setBorrowedUser(1);
        input.setBorrowedBook(2);

        assertThrows(LoanArgumentInvalidException.class, () -> service.insertLoan(input, USER_ID));
        assertEquals(List.of("lock-user", "lock-book", "count-user", "count-book"), calls);
    }

    @Test
    void insertLoanLocksInOrderAndAllowsThirdUnreturnedLoanWhenCopyIsAvailable() {
        List<String> calls = new ArrayList<>();
        Book book = new Book();
        book.setQuantity(1);
        LoanService service = createLoanService(createLoans(2), book, List.of(), calls);
        Loan input = new Loan();
        input.setBorrowedUser(1);
        input.setBorrowedBook(2);

        Loan createdLoan = service.insertLoan(input, USER_ID);

        assertEquals(42, createdLoan.getId());
        assertEquals(List.of("lock-user", "lock-book", "count-user", "count-book", "insert", "load"), calls);
    }

    private LoanRepository createExtensionRepository(Loan loan) {
        loan.setBorrowedUser(USER_ID);
        return new LoanRepository(null) {
            @Override
            public Loan getLoanByIdForUpdate(int id) {
                return id == loan.getId() ? loan : null;
            }

            @Override
            public Loan updateDueDateById(int id, LocalDate dueDate) {
                loan.setDueDate(dueDate);
                loan.setExtended(true);
                return loan;
            }
        };
    }

    private LoanService createAvailableLoanService(LoanRepository loanRepository) {
        UserRepository userRepository = new UserRepository(null) {
            @Override
            public User getUserByIdForUpdate(int id) {
                User user = new User();
                user.setId(id);
                return user;
            }
        };
        Book availableBook = new Book();
        availableBook.setQuantity(1);
        BookRepository bookRepository = new BookRepository(null) {
            @Override
            public Book getBookByIdForUpdate(int id) {
                return availableBook;
            }
        };
        return new LoanService(loanRepository, userRepository, bookRepository);
    }

    private UserRepository createUserRepository(int userId) {
        return new UserRepository(null) {
            @Override
            public User getUserByIdForUpdate(int id) {
                if (id != userId) {
                    return null;
                }
                User user = new User();
                user.setId(id);
                return user;
            }
        };
    }

    private LoanService createLoanService(List<Loan> userLoans, Book book, List<Loan> bookLoans,
                                          List<String> calls) {
        UserRepository userRepository = new UserRepository(null) {
            @Override
            public User getUserByIdForUpdate(int id) {
                calls.add("lock-user");
                User user = new User();
                user.setId(id);
                return user;
            }
        };
        BookRepository bookRepository = new BookRepository(null) {
            @Override
            public Book getBookByIdForUpdate(int id) {
                calls.add("lock-book");
                return book;
            }
        };
        Loan[] storedLoan = new Loan[1];
        LoanRepository loanRepository = new LoanRepository(null) {
            @Override
            public List<Loan> getUnReturnedLoanByUserId(int id) {
                calls.add("count-user");
                return userLoans;
            }

            @Override
            public List<Loan> getUnReturnedLoanByBookId(int id) {
                calls.add("count-book");
                return bookLoans;
            }

            @Override
            public int insertLoan(Loan loan) {
                calls.add("insert");
                loan.setId(42);
                storedLoan[0] = loan;
                return 1;
            }

            @Override
            public Loan getLoanById(int id) {
                calls.add("load");
                return storedLoan[0];
            }
        };
        return new LoanService(loanRepository, userRepository, bookRepository);
    }

    private List<Loan> createLoans(int count) {
        List<Loan> loans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            loans.add(new Loan());
        }
        return loans;
    }
}
