// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.nosql.springcloudnosqlsample;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(@RequestParam(name = "title", required = false) String title,
                                                  @RequestParam(name = "author", required = false) String author) {
        List<Book> books = new ArrayList<>();
        if (StringUtils.hasText(title)) {
            bookRepository.findByTitle(title).forEach(books::add);
        } else if (StringUtils.hasText(author)) {
            bookRepository.findByAuthor(author).forEach(books::add);
        } else {
            bookRepository.findAll().forEach(books::add);
        }

        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book created = bookRepository.save(book);
        return new ResponseEntity<>(created, HttpStatusCode.valueOf(201));
    }

    @PutMapping
    public ResponseEntity<Book> updateBook(@RequestBody Book book) {
        if (book.getId() < 1) {
            return ResponseEntity.badRequest().build();
        }
        return addBook(bookRepository.save(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable(name = "id") Long id) {
        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
