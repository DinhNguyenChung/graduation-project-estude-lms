package org.example.estudebackendspring.controller;

import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateClassSubjectRequest;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/class-subjects")
@Validated
public class ClassSubjectController {

    private final ClassSubjectService service;
    private final ClassSubjectRepository repository;

    public ClassSubjectController(ClassSubjectService service, ClassSubjectRepository repository) {
        this.service = service;
        this.repository = repository;
    }
    @GetMapping
    public List<ClassSubject> getAllClassSubjects() {
        return repository.findAll();
    }
//    @GetMapping
//    public List<ClassSubjectDTO> getAllClassSubjects() {
//        return repository.findAll().stream()
//                .map(cs -> new ClassSubjectDTO(
//                        cs.getClassSubjectId(),
//                        cs.getClazz().getName(),
//                        cs.getSubject().getName(),
//                        cs.getTeacher().getFullName()
//                ))
//                .toList();
//    }


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