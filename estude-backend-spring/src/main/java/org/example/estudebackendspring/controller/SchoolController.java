package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {
    private final SchoolRepository schoolRepository;

    public SchoolController(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @PostMapping
    public School createSchool(@RequestBody School school) {
        return schoolRepository.save(school);
    }
}
