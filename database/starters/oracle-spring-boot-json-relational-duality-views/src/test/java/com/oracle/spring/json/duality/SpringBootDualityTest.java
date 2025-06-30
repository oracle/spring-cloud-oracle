// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.oracle.spring.json.duality.builder.DualityViewScanner;
import com.oracle.spring.json.duality.model.book.Book;
import com.oracle.spring.json.duality.model.book.Loan;
import com.oracle.spring.json.duality.model.book.Member;
import com.oracle.spring.json.duality.model.employee.Employee;
import com.oracle.spring.json.duality.model.movie.Actor;
import com.oracle.spring.json.duality.model.movie.Director;
import com.oracle.spring.json.duality.model.movie.DirectorBio;
import com.oracle.spring.json.duality.model.movie.Movie;
import com.oracle.spring.json.duality.model.products.Order;
import com.oracle.spring.json.duality.model.products.Product;
import com.oracle.spring.json.duality.model.student.Student;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Disabled
public class SpringBootDualityTest {
    public static String readViewFile(String fileName) {
        try {
            File file = new ClassPathResource(Path.of("views", fileName).toString()).getFile();
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use a containerized Oracle Database instance for testing.
     */
    @Container
    @ServiceConnection
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.8-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withInitScript("products.sql")
            .withUsername("testuser")
            .withPassword("testpwd");

    @Autowired
    private DualityViewScanner dualityViewScanner;

    @Autowired
    JsonRelationalDualityClient dvClient;

    @Test
    void student() {
        Student s = new Student();
        s.setId(UUID.randomUUID().toString());
        s.setFirstName("John");
        s.setLastName("Doe");
        s.setCredits(87);
        s.setEmail("johndoe@example.com");
        s.setGpa(3.77);
        s.setMajor("Computer Science");

        int save = dvClient.save(s, Student.class);
        assertThat(save).isEqualTo(1);
        Optional<Student> byId = dvClient.findById(Student.class, s.getId());
        assertThat(byId.isPresent()).isTrue();
        assertThat(byId.get()).isEqualTo(s);
    }

    @Test
    void actor() {
        String actorId = UUID.randomUUID().toString();
        String directorId = UUID.randomUUID().toString();
        String movieId = UUID.randomUUID().toString();

        DirectorBio directorBio = new DirectorBio();
        directorBio.setDirectorId(directorId);
        directorBio.setBiography("biography");

        Director director = new Director();
        directorBio.setDirectorId(directorId);
        director.setDirectorBio(directorBio);
        director.setFirstName("John");
        director.setLastName("Doe");

        Movie m = new Movie();
        m.setMovieId(movieId);
        m.setTitle("my movie");
        m.setGenre("action");
        m.setReleaseYear(1993);
        dvClient.save(m, Movie.class);

        Actor actor = new Actor();
        actor.setActorId(actorId);
        actor.setFirstName("John");
        actor.setLastName("Doe");
        actor.setMovies(Set.of(m));

        dvClient.save(actor, Actor.class);
        Optional<Actor> actorById = dvClient.findById(Actor.class, actorId);
        assertThat(actorById.isPresent()).isTrue();
    }

    @Test
    void orders() {
        Product product = new Product();
        product.setName("my product");
        product.setPrice(100.00);

        dvClient.save(product, Product.class);
        Optional<Product> productById = dvClient.findById(Product.class, 1);
        assertThat(productById.isPresent()).isTrue();

        Order order = new Order();
        order.setProduct(productById.get());
        order.setQuantity(10);

        dvClient.save(order, Order.class);
        Optional<Order> OrderById = dvClient.findById(Order.class, 1);
        assertThat(OrderById.isPresent()).isTrue();
    }

    @Test
    void books() {
        Book book = new Book();
        book.setTitle("my book");

        dvClient.save(book, Book.class);

        Loan loan = new Loan();
        loan.setBook(book);

        Member member = new Member();
        member.setFullName("member");
        member.setLoans(List.of(loan));

        dvClient.save(member, Member.class);

        Optional<Member> byId = dvClient.findById(Member.class, 1);
        assertThat(byId.isPresent()).isTrue();
        assertThat(byId.get().getFullName()).isEqualTo("member");
        assertThat(byId.get().getLoans()).hasSize(1);
        assertThat(byId.get().getLoans().get(0).getBook().getTitle()).isEqualTo("my book");
    }

    @Test
    void employees() {
        Employee manager = new Employee();
        manager.setName("manager");

        Employee report = new Employee();
        report.setName("report");
        manager.setReports(List.of(report));
        dvClient.save(manager, Employee.class);

        Optional<Employee> byId = dvClient.findById(Employee.class, 1);
        assertThat(byId.isPresent()).isTrue();
        assertThat(byId.get().getReports()).hasSize(1);
    }
}
