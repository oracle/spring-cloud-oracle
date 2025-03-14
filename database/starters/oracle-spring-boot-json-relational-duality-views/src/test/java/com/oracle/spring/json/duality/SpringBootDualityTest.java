// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.spring.json.duality;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.oracle.spring.json.duality.builder.DualityViewScanner;
import com.oracle.spring.json.duality.model.movie.Actor;
import com.oracle.spring.json.duality.model.movie.Director;
import com.oracle.spring.json.duality.model.movie.DirectorBio;
import com.oracle.spring.json.duality.model.movie.Movie;
import com.oracle.spring.json.duality.model.student.Student;
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
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.6-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(5))
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
        DirectorBio directorBio = new DirectorBio();
        directorBio.setBiography("biography");

        Director director = new Director();
        director.setDirectorBio(directorBio);
        director.setFirstName("John");
        director.setLastName("Doe");

        Movie m = new Movie();
        m.setDirector(director);
        m.setTitle("my movie");
        m.setGenre("action");
        m.setReleaseYear(1993);

        Actor actor = new Actor();
        actor.setFirstName("John");
        actor.setLastName("Doe");
        actor.setMovies(Set.of(m));

        dvClient.save(actor, Actor.class);
        Optional<Actor> actorById = dvClient.findById(Actor.class, 1);
        assertThat(actorById.isPresent()).isTrue();
    }
}
