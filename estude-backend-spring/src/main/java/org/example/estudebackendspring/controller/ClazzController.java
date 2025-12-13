package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.ClazzDTO;
import org.example.estudebackendspring.dto.ClazzDetailDTO;
import org.example.estudebackendspring.dto.CreateClazzRequest;
import org.example.estudebackendspring.dto.TermDTO;
import org.example.estudebackendspring.dto.UpdateClazzRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.ClazzService;
import org.example.estudebackendspring.service.HomeroomService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/classes")
@Validated
public class ClazzController {

    private final ClazzService service;
    private final ClazzRepository repository;
    private final HomeroomService homeroomService;
    private final ClazzService clazzService;
    private final LogEntryService logEntryService;
    private final UserRepository userRepository;

    public ClazzController(ClazzService service, ClazzRepository repository, HomeroomService homeroomService,
                           ClazzService clazzService, LogEntryService logEntryService, UserRepository userRepository) {
        this.service = service;
        this.repository = repository;
        this.homeroomService = homeroomService;
        this.clazzService = clazzService;
        this.logEntryService = logEntryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ClazzDTO> GettAllClazz() {
        return clazzService.getAllClasses();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ClazzDTO> createClazz(@Valid @RequestBody CreateClazzRequest req) {
        Clazz created = service.createClazz(req);
        
        // Log class creation
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                User currentUser = (User) principal;
                logEntryService.createLog(
                    "Clazz",
                    created.getClassId(),
                    "Tạo lớp học mới: " + created.getName() + " (Khối " + created.getGradeLevel() + ")",
                    ActionType.CREATE,
                    created.getSchool() != null ? created.getSchool().getSchoolId() : null,
                    "School",
                    currentUser
                );
            }
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log class creation: " + e.getMessage());
        }
        
        // Convert to DTO to avoid lazy loading issues
        ClazzDTO dto = service.convertToDTO(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{classId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ClazzDetailDTO> getClazz(@PathVariable Long classId) {
        ClazzDetailDTO dto = service.getClazzDetail(classId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{classId}")
    @Transactional
    public ResponseEntity<ClazzDTO> updateClazz(@PathVariable Long classId,
                                             @Valid @RequestBody UpdateClazzRequest req) {
        Clazz updated = service.updateClazz(classId, req);
        
        // Log class update
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                User currentUser = (User) principal;
                logEntryService.createLog(
                    "Clazz",
                    classId,
                    "Cập nhật thông tin lớp: " + updated.getName(),
                    ActionType.UPDATE,
                    updated.getSchool() != null ? updated.getSchool().getSchoolId() : null,
                    "School",
                    currentUser
                );
            }
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log class update: " + e.getMessage());
        }
        
        // Convert to DTO to avoid lazy loading issues
        ClazzDTO dto = service.convertToDTO(updated);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClazz(@PathVariable Long classId) {
        // Get class info before deletion for logging
        Clazz clazz = null;
        try {
            clazz = repository.findById(classId).orElse(null);
        } catch (Exception e) {
            // Continue with deletion even if we can't get class info
        }
        
        service.deleteClazz(classId);
        
        // Log class deletion
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                User currentUser = (User) principal;
                String className = clazz != null ? clazz.getName() : "Unknown";
                logEntryService.createLog(
                    "Clazz",
                    classId,
                    "Xóa lớp học: " + className,
                    ActionType.DELETE,
                    clazz != null && clazz.getSchool() != null ? clazz.getSchool().getSchoolId() : null,
                    "School",
                    currentUser
                );
            }
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log class deletion: " + e.getMessage());
        }
        
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/school/{schoolId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<ClazzDTO>> getClassesBySchool(@PathVariable Long schoolId) {
        List<Clazz> classes = clazzService.getClassesBySchool(schoolId);
        
        // Convert to DTOs to avoid lazy loading issues
        List<ClazzDTO> dtos = classes.stream()
            .map(clazz -> {
                List<TermDTO> termDTOs = null;
                if (clazz.getTerms() != null) {
                    termDTOs = clazz.getTerms().stream()
                        .map(term -> new TermDTO(
                            term.getTermId(),
                            term.getName(),
                            term.getBeginDate(),
                            term.getEndDate()
                        ))
                        .toList();
                }
                
                return new ClazzDTO(
                    clazz.getClassId(),
                    clazz.getName(),
                    clazz.getGradeLevel(),
                    clazz.getClassSize(),
                    clazz.getHomeroomTeacher() != null ? clazz.getHomeroomTeacher().getUserId() : null,
                    clazz.getHomeroomTeacher() != null ? clazz.getHomeroomTeacher().getFullName() : null,
                    clazz.getSchool() != null ? clazz.getSchool().getSchoolId() : null,
                    clazz.getSchool() != null ? clazz.getSchool().getSchoolName() : null,
                    termDTOs
                );
            })
            .toList();
        
        return ResponseEntity.ok(dtos);
    }
    /**
     * Thêm GVCN cho lớp
     * Header: X-User-Id = id người thao tác (Admin hoặc Teacher có isAdmin=true)
     * Body param: teacherId (query hoặc request param)
     */
    @PostMapping("/{classId}/homeroom-teacher")
    public ResponseEntity<Clazz> addHomeroomTeacher(@RequestHeader("X-User-Id") Long actingUserId,
                                                    @PathVariable Long classId,
                                                    @RequestParam Long teacherId) {
        Clazz updated = homeroomService.assignHomeroomTeacher(actingUserId, classId, teacherId);
        
        // Log homeroom teacher assignment
        try {
            String teacherName = updated.getHomeroomTeacher() != null ? updated.getHomeroomTeacher().getFullName() : "Unknown";
            User user = userRepository.findById(actingUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));

            if (actingUserId != null) {
                logEntryService.createLog(
                        "Clazz",
                        classId,

                        "Bổ nhiệm GVCN cho lớp " + updated.getName() + ": " + teacherName,
                        ActionType.UPDATE,
                        updated.getSchool() != null ? updated.getSchool().getSchoolId() : null,
                        "School",
                        user
                );
            }
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log homeroom teacher assignment: " + e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    /**
     * Xóa GVCN khỏi lớp
     */
    @DeleteMapping("/{classId}/homeroom-teacher")
    public ResponseEntity<Void> removeHomeroomTeacher(@RequestHeader("X-User-Id") Long actingUserId,
                                                      @PathVariable Long classId) {
        // Get class info before removal for logging
        Clazz clazz = null;
        String teacherName = null;
        try {
            clazz = repository.findById(classId).orElse(null);
            teacherName = clazz != null && clazz.getHomeroomTeacher() != null ? 
                         clazz.getHomeroomTeacher().getFullName() : "Unknown";
        } catch (Exception e) {
            // Continue with removal even if we can't get info
        }
        
        homeroomService.removeHomeroomTeacher(actingUserId, classId);
        
        // Log homeroom teacher removal
        try {
            User user = userRepository.findById(actingUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            String className = clazz != null ? clazz.getName() : "Unknown";
            logEntryService.createLog(
                "Clazz",
                classId,
                "Gỡ bỏ GVCN khỏi lớp " + className + ": " + (teacherName != null ? teacherName : "Unknown"),
                ActionType.UPDATE,
                clazz != null && clazz.getSchool() != null ? clazz.getSchool().getSchoolId() : null,
                    "School",
                    user
            );
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log homeroom teacher removal: " + e.getMessage());
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật (thay) GVCN cho lớp
     */
    @PutMapping("/{classId}/homeroom-teacher")
    public ResponseEntity<Clazz> updateHomeroomTeacher(@RequestHeader("X-User-Id") Long actingUserId,
                                                       @PathVariable Long classId,
                                                       @RequestParam Long newTeacherId) {
        Clazz updated = homeroomService.updateHomeroomTeacher(actingUserId, classId, newTeacherId);
        
        // Log homeroom teacher update
        try {
            User user = userRepository.findById(actingUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            String newTeacherName = updated.getHomeroomTeacher() != null ? updated.getHomeroomTeacher().getFullName() : "Unknown";
            logEntryService.createLog(
                "Clazz",
                classId,
                "Thay đổi GVCN cho lớp " + updated.getName() + ": " + newTeacherName,
                ActionType.UPDATE,
                updated.getSchool() != null ? updated.getSchool().getSchoolId() : null,
                "School",
                user
            );
        } catch (Exception e) {
            // Log warning but don't fail the main operation
            System.err.println("Failed to log homeroom teacher update: " + e.getMessage());
        }
        
        return ResponseEntity.ok(updated);
    }
//
}
