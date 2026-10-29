package com.example.bookmanagement.controller;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.form.BookForm;
import com.example.bookmanagement.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getBooks() {
        return ResponseEntity.ok(bookService.getBooksByKeyword(""));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable int id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<Book>> getBooksByKeyword(@PathVariable String keyword) {
        return ResponseEntity.ok(bookService.getBooksByKeyword(keyword));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooksByKeyword(@RequestParam(defaultValue = "") String keyword) {
        return ResponseEntity.ok(bookService.getAvailableBooksByKeyword(keyword));
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable String author) {
        return ResponseEntity.ok(bookService.getBooksByAuthor(author));
    }

    @PostMapping
    public ResponseEntity<Book> insertBook(@RequestBody BookForm bookForm, UriComponentsBuilder uriComponentsBuilder) {
        Book insertedBook = bookService.insertBook(bookForm);
        URI location = uriComponentsBuilder.path("/api/books/{id}").buildAndExpand(insertedBook.getId()).toUri();

        return ResponseEntity.created(location).body(insertedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@RequestBody BookForm bookForm, @PathVariable int id) {
        Book updatedBook = bookService.updateBook(bookForm, id);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteBookById(@PathVariable int id) {
        bookService.softDeleteBookById(id);
        return ResponseEntity.noContent().build();
    }

}
