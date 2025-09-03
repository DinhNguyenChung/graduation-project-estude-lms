package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateClazzRequest;
import org.example.estudebackendspring.dto.UpdateClazzRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.service.ClazzService;
import org.example.estudebackendspring.service.HomeroomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ClazzController(ClazzService service, ClazzRepository repository, HomeroomService homeroomService, ClazzService clazzService) {
        this.service = service;
        this.repository = repository;
        this.homeroomService = homeroomService;
        this.clazzService = clazzService;
    }

    @GetMapping
    public List<Clazz> GettAllClazz(){
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Clazz> createClazz(@Valid @RequestBody CreateClazzRequest req) {
        Clazz created = service.createClazz(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{classId}")
    public ResponseEntity<Clazz> getClazz(@PathVariable Long classId) {
        Clazz c = service.getClazz(classId);
        return ResponseEntity.ok(c);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<Clazz> updateClazz(@PathVariable Long classId,
                                             @Valid @RequestBody UpdateClazzRequest req) {
        Clazz updated = service.updateClazz(classId, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClazz(@PathVariable Long classId) {
        service.deleteClazz(classId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<Clazz>> getClassesBySchool(@PathVariable Long schoolId) {
        List<Clazz> classes = clazzService.getClassesBySchool(schoolId);
        return ResponseEntity.ok(classes);
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
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    /**
     * Xóa GVCN khỏi lớp
     */
    @DeleteMapping("/{classId}/homeroom-teacher")
    public ResponseEntity<Void> removeHomeroomTeacher(@RequestHeader("X-User-Id") Long actingUserId,
                                                      @PathVariable Long classId) {
        homeroomService.removeHomeroomTeacher(actingUserId, classId);
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
        return ResponseEntity.ok(updated);
    }
//
}
