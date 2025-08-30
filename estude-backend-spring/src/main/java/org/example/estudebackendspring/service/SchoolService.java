package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SchoolService {
    private final SchoolRepository schoolRepository;


    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }
    public Optional<School> getSchoolById(Long schoolId) {
        return schoolRepository.findById(schoolId);
    }
}
