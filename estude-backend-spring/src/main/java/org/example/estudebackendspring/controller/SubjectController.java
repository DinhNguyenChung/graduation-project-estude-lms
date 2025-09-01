package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateSubjectRequest;
import org.example.estudebackendspring.dto.UpdateSubjectRequest;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.example.estudebackendspring.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/subjects")
@Validated
public class SubjectController {

    private final SubjectService service;
    private final SubjectRepository repository;

    public SubjectController(SubjectService service, SubjectRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody CreateSubjectRequest req) {
        Subject created = service.createSubject(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @GetMapping
    public List<Subject> getAllSubjects() {
        return repository.findAll();
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<Subject> getSubject(@PathVariable Long subjectId) {
        Subject s = service.getSubject(subjectId);
        return ResponseEntity.ok(s);
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long subjectId,
                                                 @Valid @RequestBody UpdateSubjectRequest req) {
        Subject updated = service.updateSubject(subjectId, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long subjectId) {
        service.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }
}