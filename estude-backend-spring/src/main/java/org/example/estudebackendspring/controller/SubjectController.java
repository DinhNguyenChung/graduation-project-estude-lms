package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.CreateSubjectRequest;
import org.example.estudebackendspring.dto.SubjectDTO;
import org.example.estudebackendspring.dto.UpdateSubjectRequest;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.enums.GradeLevel;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.example.estudebackendspring.service.SubjectService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/subjects")
@Validated
@Slf4j
public class SubjectController {

    private final SubjectService service;
    private final SubjectRepository repository;
    private final SubjectService subjectService;
    private final LogEntryService logEntryService;

    public SubjectController(SubjectService service, SubjectRepository repository, 
                            SubjectService subjectService, LogEntryService logEntryService) {
        this.service = service;
        this.repository = repository;
        this.subjectService = subjectService;
        this.logEntryService = logEntryService;
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody CreateSubjectRequest req) {
        Subject created = service.createSubject(req);
        
        // Tạo log entry
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logEntryService.createLog(
                    "Subject",
                    created.getSubjectId(),
                    "Tạo mới môn học: " + created.getName(),
                    ActionType.CREATE,
                    null,
                    "",
                   currentUser
            );
        } catch (Exception e) {
            log.warn("Failed to log subject creation", e);
        }
        
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
        
        // Tạo log entry
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logEntryService.createLog(
                    "Subject",
                    subjectId,
                    "Cập nhật môn học: " + updated.getName() ,
                    ActionType.UPDATE,
                    null,
                    "",
                    currentUser
            );
        } catch (Exception e) {
            log.warn("Failed to log subject update", e);
        }
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long subjectId) {
        // Lấy thông tin subject trước khi xóa
        Subject subject = null;
        try {
            subject = service.getSubject(subjectId);
        } catch (Exception e) {
            log.warn("Could not get subject info before deletion", e);
        }
        
        service.deleteSubject(subjectId);
        
        // Tạo log entry
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String subjectInfo = subject != null ? 
                    subject.getName() + " (Mã: " + subject.getSubjectId() + ")" :
                    "ID: " + subjectId;
            logEntryService.createLog(
                    "Subject",
                    subjectId,
                    "Xóa môn học: " + subjectInfo,
                    ActionType.DELETE,
                    null,
                    "",
                    currentUser
            );
        } catch (Exception e) {
            log.warn("Failed to log subject deletion", e);
        }
        
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/by-class/{classId}")
    public ResponseEntity<List<Subject>> getSubjectsByClass(@PathVariable Long classId) {
        List<Subject> subjects = subjectService.getSubjectsByClassId(classId);
        if (subjects.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(subjects); // 200
    }
    
    // Note: This endpoint is commented out because Subject-School relationship has been removed
    // Use the new grade-level based filtering instead: /api/subjects/by-grade?gradeLevel=GRADE_10
    /*
    @GetMapping("/by-school/{schoolId}")
    public ResponseEntity<List<Subject>> getSubjectsBySchool(@PathVariable Long schoolId) {
        List<Subject> subjects = subjectService.getAllSubjectsBySchoolId(schoolId);
        if (subjects.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(subjects); // 200
    }
    */
    
    // Grade level and volume filtering has been moved to Topic level.
    // Use TopicController endpoints instead:
    // - GET /api/topics?subjectId=X&gradeLevel=Y&volume=Z
    // - GET /api/topics/grades?subjectId=X
    // - GET /api/topics/volumes?subjectId=X&gradeLevel=Y

}