package org.example.estudebackendspring.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                                null // schools relationship has been removed from Subject entity
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
            System.err.println("Failed to log class-subject removal: " + e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }
    
    /**
     * PATCH /api/class-subjects/{classSubjectId}/teacher
     * Update teacher for an existing ClassSubject
     * This solves the 409 Conflict issue when editing teacher in Frontend
     */
    @PatchMapping("/{classSubjectId}/teacher")
    public ResponseEntity<?> updateTeacher(
            @PathVariable Long classSubjectId,
            @RequestBody UpdateTeacherRequest request
    ) {
        try {
            // Update teacher
            ClassSubject updated = service.updateTeacher(classSubjectId, request.getTeacherId());
            
            // Log the update
            try {
                User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String oldTeacherName = updated.getTeacher() != null ? updated.getTeacher().getFullName() : "Không có";
                String newTeacherInfo = request.getTeacherId() != null ? 
                        " → " + updated.getTeacher().getFullName() : 
                        " → Xóa giáo viên";
                
                logEntryService.createLog(
                        "ClassSubject",
                        classSubjectId,
                        "Cập nhật giáo viên môn " + updated.getSubject().getName() +
                                " lớp " + updated.getTerm().getClazz().getName() + newTeacherInfo,
                        ActionType.UPDATE,
                        updated.getTerm().getTermId(),
                        "Term",
                        currentUser
                );
            } catch (Exception e) {
                System.err.println("⚠ Failed to log teacher update: " + e.getMessage());
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("classSubjectId", updated.getClassSubjectId());
            response.put("classId", updated.getTerm().getClazz().getClassId());
            response.put("subjectId", updated.getSubject().getSubjectId());
            response.put("teacherId", updated.getTeacher() != null ? updated.getTeacher().getUserId() : null);
            response.put("teacherName", updated.getTeacher() != null ? updated.getTeacher().getFullName() : null);
            response.put("termId", updated.getTerm().getTermId());
            
            // Add subject and teacher details
            Map<String, Object> subjectDetail = new HashMap<>();
            subjectDetail.put("subjectId", updated.getSubject().getSubjectId());
            subjectDetail.put("name", updated.getSubject().getName());
            response.put("subject", subjectDetail);
            
            if (updated.getTeacher() != null) {
                Map<String, Object> teacherDetail = new HashMap<>();
                teacherDetail.put("userId", updated.getTeacher().getUserId());
                teacherDetail.put("fullName", updated.getTeacher().getFullName());
                teacherDetail.put("teacherCode", updated.getTeacher().getTeacherCode());
                response.put("teacher", teacherDetail);
            } else {
                response.put("teacher", null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}