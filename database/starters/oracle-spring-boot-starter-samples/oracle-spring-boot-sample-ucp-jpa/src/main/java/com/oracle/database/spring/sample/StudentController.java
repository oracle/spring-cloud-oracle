// Copyright (c) 2024, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.database.spring.sample;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public ResponseEntity<List<Student>> listStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student created = studentRepository.save(student);
        return new ResponseEntity<>(created, HttpStatusCode.valueOf(201));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable String studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        return student.map(ResponseEntity::ok).orElseGet(() ->
                ResponseEntity.notFound().build()
        );
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<?> deleteStudent(@PathVariable String studentId) {
        studentRepository.deleteById(studentId);
        return ResponseEntity.noContent().build();
    }
}

