// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
package com.oracle.cloud.spring.sample.nosql.springcloudnosqlsample;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class NoSQLSampleApplicationIT {
    @Autowired
    private BookController bookController;

    @Autowired
    BookRepository bookRepository;

    @Test
    void bookControllerTest() {
        // clear the book database before running the test
        bookRepository.deleteAll();
        // Create three Book objects
        Book book1 = new Book();
        book1.setTitle("To Kill a Mockingbird");
        book1.setAuthor("Harper Lee");
        book1.setPrice(12.99);

        Book book2 = new Book();
        book2.setTitle("1984");
        book2.setAuthor("George Orwell");
        book2.setPrice(10.99);

        Book book3 = new Book();
        book3.setTitle("Animal Farm");
        book3.setAuthor("George Orwell");  // Same author as book2
        book3.setPrice(9.99);

        Book created1 = bookController.addBook(book1).getBody();
        Book created2 = bookController.addBook(book2).getBody();
        Book created3 = bookController.addBook(book3).getBody();

        // Query books based on author
        List<Book> georgeOrwellBooks = bookController.getAllBooks(null, "George Orwell").getBody();
        assertThat(georgeOrwellBooks).hasSize(2);

        // Query books based on title
        List<Book> titleQueryBooks = bookController.getAllBooks("To Kill a Mockingbird", null).getBody();
        assertThat(titleQueryBooks).hasSize(1);
        assertThat(titleQueryBooks.get(0)).isEqualTo(created1);

        // Update book
        assertThat(created3).isNotNull();
        created3.setPrice(25.00);
        Book updated3 = bookController.updateBook(created3).getBody();
        assertThat(updated3).isNotNull();
        assertThat(updated3.getPrice()).isEqualTo(25.0);

        // Delete book and verify it's gone
        assertThat(created2).isNotNull();
        bookController.deleteBook(created2.getId());
        List<Book> title1984Books = bookController.getAllBooks("1984", null).getBody();
        assertThat(title1984Books).hasSize(0);
    }
}
