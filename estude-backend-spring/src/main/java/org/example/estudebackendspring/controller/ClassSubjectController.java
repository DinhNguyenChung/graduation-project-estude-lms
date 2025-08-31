package org.example.estudebackendspring.controller;

import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateClassSubjectRequest;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/class-subjects")
@Validated
public class ClassSubjectController {

    private final ClassSubjectService service;

    public ClassSubjectController(ClassSubjectService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClassSubject> assignSubject(@Valid @RequestBody CreateClassSubjectRequest req) {
        ClassSubject created = service.assignSubjectToClass(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{classSubjectId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long classSubjectId) {
        service.removeClassSubject(classSubjectId);
        return ResponseEntity.noContent().build();
    }
}