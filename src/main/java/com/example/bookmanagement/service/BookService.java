package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.exception.BookArgumentInvalidException;
import com.example.bookmanagement.exception.BookNotFoundException;
import com.example.bookmanagement.form.BookForm;
import com.example.bookmanagement.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book getBookById(int id) {
        Book book = bookRepository.getBookById(id);
        if (book == null) {
            throw new BookNotFoundException("本が見つかりません");
        }
        return book;
    }

    public List<Book> getBooksByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BookArgumentInvalidException("キーワードが空です");
        }
        return bookRepository.getBooksByKeyword(keyword);
    }

    public List<Book> getAvailableBooksByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BookArgumentInvalidException("キーワードが空です");
        }
        return bookRepository.getAvailableBooksByKeyword(keyword);
    }

    public List<Book> getAllBooks() {
        return bookRepository.getAllBooks();
    }

    public List<Book> getBooksByAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new BookArgumentInvalidException("著者が空なため本が見つかりませんでした");
        }
        return bookRepository.getBooksByAuthor(author);
    }

    public Book getBookByIdForUpdate(int id) {
        Book book = bookRepository.getBookByIdForUpdate(id);
        if (book == null) {
            throw new BookNotFoundException("本が見つかりません");
        }
        return book;
    }

    public Book insertBook(BookForm bookForm) {
        if (bookForm == null ||
                bookForm.getName() == null || bookForm.getName().isBlank() ||
                bookForm.getAuthor() == null || bookForm.getAuthor().isBlank() ||
                bookForm.getQuantity() < 0) {
            throw new BookArgumentInvalidException("本の情報が不正です");
        }
        Book book = new Book();
        book.setName(bookForm.getName());
        book.setAuthor(bookForm.getAuthor());
        book.setIsbn(bookForm.getIsbn() == null ? "" : bookForm.getIsbn());
        book.setQuantity(bookForm.getQuantity());
        if (bookRepository.insertBook(book) != 1) {
            throw new RuntimeException("本の登録に失敗しました");
        }
        return bookRepository.getBookById(book.getId());
    }

    @Transactional
    public Book updateBook(BookForm bookForm, int id) {
        if (bookForm == null ||
                bookForm.getName() == null || bookForm.getName().isBlank() ||
                bookForm.getAuthor() == null || bookForm.getAuthor().isBlank() ||
                bookForm.getQuantity() < 0) {
            throw new BookArgumentInvalidException("本の情報が不正です");
        }
        Book book = getBookByIdForUpdate(id);
        book.setName(bookForm.getName());
        book.setAuthor(bookForm.getAuthor());
        book.setIsbn(bookForm.getIsbn() == null ? "" : bookForm.getIsbn());
        book.setQuantity(bookForm.getQuantity());
        return bookRepository.updateBook(book);
    }

    @Transactional
    public boolean softDeleteBookById(int id) {
        if (getBookByIdForUpdate(id).getQuantity() != 0) {
            throw new BookArgumentInvalidException("削除しようとしている本の在庫がまだあります");
        }

        if (!bookRepository.softDeleteBookById(id)) {
            throw new BookNotFoundException("本が見つかりません");
        }
        return true;
    }
}
