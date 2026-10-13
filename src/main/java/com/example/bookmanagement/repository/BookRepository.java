package com.example.bookmanagement.repository;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.mapper.BookMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepository {
    private final BookMapper bookMapper;

    public BookRepository(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public Book getBookById(int id) {
        return bookMapper.getBookById(id);
    }

    public List<Book> getBooksByKeyword(String keyword) {
        return bookMapper.getBooksByKeyword(keyword);
    }

    public List<Book> getAvailableBooksByKeyword(String keyword) {
        return bookMapper.getAvailableBooksByKeyword(keyword);
    }

    public List<Book> getAllBooks() {
        return bookMapper.getAllBooks();
    }

    public List<Book> getBooksByAuthor(String author) {
        return bookMapper.getBooksByAuthor(author);
    }

    public Book getBookByIdForUpdate(int id) {
        return bookMapper.getBookByIdForUpdate(id);
    }

    public int insertBook(Book book) {
        return bookMapper.insertBook(book);
    }

    public Book updateBook(Book book) {
        if (bookMapper.updateBook(book) == 1) {
            return book;
        }
        return null;
    }

    public boolean softDeleteBookById(int id) {
        return bookMapper.softDeleteBookById(id) > 0;
    }
}
