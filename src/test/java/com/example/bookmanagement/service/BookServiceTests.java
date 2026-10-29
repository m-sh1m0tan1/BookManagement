package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.exception.BookArgumentInvalidException;
import com.example.bookmanagement.exception.BookNotFoundException;
import com.example.bookmanagement.form.BookForm;
import com.example.bookmanagement.repository.BookRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookServiceTests {
    @Test
    void insertBookReturnsGeneratedIdAndStoredAvailability() {
        BookRepository repository = new BookRepository(null) {
            @Override
            public int insertBook(Book book) {
                book.setId(42);
                return 1;
            }

            @Override
            public Book getBookById(int id) {
                if (id != 42) {
                    return null;
                }
                Book storedBook = new Book();
                storedBook.setId(id);
                storedBook.setAvailable(true);
                return storedBook;
            }
        };
        BookForm form = new BookForm();
        form.setName("Sample Book");
        form.setAuthor("Sample Author");
        form.setQuantity(1);

        Book createdBook = new BookService(repository).insertBook(form);

        assertEquals(42, createdBook.getId());
        assertTrue(createdBook.isAvailable());
    }

    @Test
    void insertBookRejectsWhitespaceOnlyRequiredFields() {
        BookService service = createValidationService();

        assertThrows(BookArgumentInvalidException.class,
                () -> service.insertBook(createForm("   ", "Sample Author")));
        assertThrows(BookArgumentInvalidException.class,
                () -> service.insertBook(createForm("Sample Book", "   ")));
    }

    @Test
    void updateBookRejectsWhitespaceOnlyRequiredFields() {
        BookService service = createValidationService();

        assertThrows(BookArgumentInvalidException.class,
                () -> service.updateBook(createForm("   ", "Sample Author"), 42));
        assertThrows(BookArgumentInvalidException.class,
                () -> service.updateBook(createForm("Sample Book", "   "), 42));
    }

    @Test
    void insertBookRejectsFieldsLongerThanDatabaseLimits() {
        BookService service = createValidationService();
        BookForm longName = createForm("n".repeat(256), "Sample Author");
        BookForm longAuthor = createForm("Sample Book", "a".repeat(256));
        BookForm longIsbn = createForm("Sample Book", "Sample Author");
        longIsbn.setIsbn("1".repeat(14));

        assertThrows(BookArgumentInvalidException.class, () -> service.insertBook(longName));
        assertThrows(BookArgumentInvalidException.class, () -> service.insertBook(longAuthor));
        assertThrows(BookArgumentInvalidException.class, () -> service.insertBook(longIsbn));
    }

    @Test
    void insertBookRejectsWhitespaceOnlyIsbnLongerThanDatabaseLimit() {
        BookService service = createValidationService();
        BookForm form = createForm("Sample Book", "Sample Author");
        form.setIsbn(" ".repeat(14));

        assertThrows(BookArgumentInvalidException.class, () -> service.insertBook(form));
    }

    @Test
    void getBooksByKeywordRejectsWhitespaceOnlyKeyword() {
        assertThrows(BookArgumentInvalidException.class,
                () -> createSearchService().getBooksByKeyword("   "));
    }

    @Test
    void getAvailableBooksByKeywordRejectsWhitespaceOnlyKeyword() {
        assertThrows(BookArgumentInvalidException.class,
                () -> createSearchService().getAvailableBooksByKeyword("   "));
    }

    @Test
    void getBooksByAuthorRejectsWhitespaceOnlyAuthor() {
        assertThrows(BookArgumentInvalidException.class,
                () -> createSearchService().getBooksByAuthor("   "));
    }

    @Test
    void softDeleteBookRejectsMissingBook() {
        BookService service = new BookService(createDeletionRepository(null));

        assertThrows(BookNotFoundException.class, () -> service.softDeleteBookById(42));
    }

    @Test
    void softDeleteBookRejectsBookWithRemainingQuantity() {
        Book book = new Book();
        book.setQuantity(1);
        BookService service = new BookService(createDeletionRepository(book));

        assertThrows(BookArgumentInvalidException.class, () -> service.softDeleteBookById(42));
    }

    private BookRepository createDeletionRepository(Book book) {
        return new BookRepository(null) {
            @Override
            public Book getBookByIdForUpdate(int id) {
                return book;
            }

            @Override
            public boolean softDeleteBookById(int id) {
                return true;
            }
        };
    }

    private BookService createValidationService() {
        BookRepository repository = new BookRepository(null) {
            @Override
            public int insertBook(Book book) {
                return 1;
            }

            @Override
            public Book getBookById(int id) {
                return new Book();
            }

            @Override
            public Book getBookByIdForUpdate(int id) {
                return new Book();
            }

            @Override
            public Book updateBook(Book book) {
                return book;
            }
        };
        return new BookService(repository);
    }

    private BookService createSearchService() {
        BookRepository repository = new BookRepository(null) {
            @Override
            public List<Book> getBooksByKeyword(String keyword) {
                return List.of();
            }

            @Override
            public List<Book> getAvailableBooksByKeyword(String keyword) {
                return List.of();
            }

            @Override
            public List<Book> getBooksByAuthor(String author) {
                return List.of();
            }
        };
        return new BookService(repository);
    }

    private BookForm createForm(String name, String author) {
        BookForm form = new BookForm();
        form.setName(name);
        form.setAuthor(author);
        form.setQuantity(1);
        return form;
    }
}
