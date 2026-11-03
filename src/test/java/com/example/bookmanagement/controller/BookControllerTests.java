package com.example.bookmanagement.controller;

import com.example.bookmanagement.service.BookService;
import com.example.bookmanagement.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTests {
    @Test
    void getAvailableBooksWithoutKeywordSearchesWithEmptyKeyword() throws Exception {
        SearchService service = new SearchService();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BookController(service)).build();

        mvc.perform(get("/api/books/available"))
                .andExpect(status().isOk());

        assertEquals("available:", service.search);
    }

    @Test
    void getAvailableBooksWithKeywordPassesKeywordToService() throws Exception {
        SearchService service = new SearchService();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BookController(service)).build();

        mvc.perform(get("/api/books/available").param("keyword", "Spring"))
                .andExpect(status().isOk());

        assertEquals("available:Spring", service.search);
    }

    @Test
    void getBooksWithoutFiltersSearchesAllBooks() throws Exception {
        SearchService service = new SearchService();
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BookController(service)).build();

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk());

        assertEquals("all:", service.search);
    }

    private static class SearchService extends BookService {
        private String search;

        SearchService() {
            super(null);
        }

        @Override
        public List<Book> getBooksByKeyword(String keyword) {
            search = "all:" + keyword;
            return List.of();
        }

        @Override
        public List<Book> getAvailableBooksByKeyword(String keyword) {
            search = "available:" + keyword;
            return List.of();
        }
    }
}
