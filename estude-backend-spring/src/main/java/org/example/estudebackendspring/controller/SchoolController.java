package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.example.estudebackendspring.service.SchoolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {
    private final SchoolRepository schoolRepository;
    private final SchoolService schoolService;

    public SchoolController(SchoolRepository schoolRepository, SchoolService schoolService) {
        this.schoolRepository = schoolRepository;
        this.schoolService = schoolService;
    }

    @PostMapping
    public School createSchool(@RequestBody School school) {
        return schoolRepository.save(school);
    }
    @GetMapping("/{schoolId}")
    public ResponseEntity<?> getSchoolById(@PathVariable Long schoolId) {
        return schoolService.getSchoolById(schoolId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }



}
