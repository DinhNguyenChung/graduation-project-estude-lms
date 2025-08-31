package org.example.estudebackendspring.controller;


import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateClazzRequest;
import org.example.estudebackendspring.dto.UpdateClazzRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.service.ClazzService;
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

    public ClazzController(ClazzService service, ClazzRepository repository) {
        this.service = service;
        this.repository = repository;
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
}
