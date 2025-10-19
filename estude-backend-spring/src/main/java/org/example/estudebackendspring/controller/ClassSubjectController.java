package org.example.estudebackendspring.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final LogEntryService logEntryService;

    public ClassSubjectController(ClassSubjectService service, ClassSubjectRepository repository, LogEntryService logEntryService) {
        this.service = service;
        this.repository = repository;
        this.logEntryService = logEntryService;
    }
//    @GetMapping
//    public List<ClassSubject> getAllClassSubjects() {
//        return repository.findAll();
//    }
    @GetMapping("/{classSubjectId}")
    public ResponseEntity<ClassSubjectResponse> getClassSubjectById(@PathVariable("classSubjectId") Long id) {
        ClassSubjectResponse dto = service.getClassSubjectByClassSubjectId(id);
        return ResponseEntity.ok(dto);
    }
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
                                cs.getTeacher().getUserId(),
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
//        Tạo log
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            for (ClassSubject cs : created) {
                logEntryService.createLog(
                        "ClassSubject",
                        cs.getClassSubjectId(),
                        "Gán môn học " + cs.getSubject().getName() +
                                " cho lớp " + cs.getTerm().getClazz().getName(),
                        ActionType.CREATE,
                        cs.getTerm().getTermId(),
                        "Term",
                        currentUser
                );
            }
        } catch (Exception e) {
            // Không để lỗi log làm fail API
            System.err.println("⚠Failed to log class-subject assignment: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @DeleteMapping("/{classSubjectId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long classSubjectId) {
        try {
            // Lấy thông tin trước khi xóa để ghi log
            ClassSubject removed = service.getClassSubjectById(classSubjectId);

            service.removeClassSubject(classSubjectId);

            // Tạo log sau khi xóa
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logEntryService.createLog(
                    "ClassSubject",
                    classSubjectId,
                    "Xóa môn học " + removed.getSubject().getName() +
                            " khỏi lớp " + removed.getTerm().getClazz().getName(),
                    ActionType.DELETE,
                    removed.getTerm().getTermId(),
                    "Term",
                    currentUser
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("⚠Failed to log class-subject removal: " + e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }
}