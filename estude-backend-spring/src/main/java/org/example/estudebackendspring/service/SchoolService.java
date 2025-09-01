package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public List<School> getAllClazzBySchoolId(Long schoolId) {
        return schoolRepository.getClazzBySchoolId(schoolId);
    }
    public List<School> getAllSchoolBySchoolCode(String schoolCode
    ) {
        return schoolRepository.getClazzBySchoolCode(schoolCode);
    }
}
