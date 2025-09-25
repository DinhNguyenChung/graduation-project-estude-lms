package org.example.estudebackendspring.controller;

import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


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
//    @GetMapping
//    public List<ClassSubject> getAllClassSubjects() {
//        return repository.findAll();
//    }
    @GetMapping
    public List<ClazzSubjectsDTO> getAllClassSubjectsDTO() {
        return repository.findAll().stream()
                .map(cs -> new ClazzSubjectsDTO(
                        cs.getClassSubjectId(),
                        cs.getTerm() != null ? new TermDTO(
                                cs.getTerm().getTermId(),
                                cs.getTerm().getName(),
                                cs.getTerm().getBeginDate(),
                                cs.getTerm().getEndDate()
                        ) : null,
                        cs.getSubject() != null ? new SubjectClazzDTO(
                                cs.getSubject().getSubjectId(),
                                cs.getSubject().getName(),
                                cs.getSubject().getDescription(),
                                cs.getSubject().getSchools() !=null ?
                                        cs.getSubject().getSchools().stream().map(s -> new SchoolDTO(
                                                s.getSchoolId(),
                                                s.getSchoolCode(),
                                                s.getSchoolName()
                                        ))
                                                .collect(Collectors.toList()):null
                        ) : null,
                        cs.getTeacher() != null ? new TeacherDTO(
                                cs.getTeacher().getTeacherCode(),
                                cs.getTeacher().getFullName(),
                                cs.getTeacher().getHireDate(),
                                cs.getTeacher().getEndDate(),
                                cs.getTeacher().isAdmin(),
                                cs.getTeacher().isHomeroomTeacher()
                        ) : null,
                        cs.getTerm() != null && cs.getTerm().getClazz() != null ? cs.getTerm().getClazz().getClassId() : null,
                        cs.getTerm() != null && cs.getTerm().getClazz() != null ? cs.getTerm().getClazz().getName() : null,
                        cs.getTerm() != null && cs.getTerm().getClazz() != null ? cs.getTerm().getClazz().getGradeLevel() : null
                ))
                .toList();
    }


    @PostMapping
    public ResponseEntity<List<ClassSubject>> assignSubject(@Valid @RequestBody CreateClassSubjectRequest req) {
        List<ClassSubject> created = service.assignSubjectToClass(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @DeleteMapping("/{classSubjectId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long classSubjectId) {
        service.removeClassSubject(classSubjectId);
        return ResponseEntity.noContent().build();
    }
}