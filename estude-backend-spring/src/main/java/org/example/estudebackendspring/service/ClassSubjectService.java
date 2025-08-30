package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;

    public ClassSubjectService(ClassSubjectRepository classSubjectRepository) {
        this.classSubjectRepository = classSubjectRepository;
    }

    public List<ClassSubject> getClassSubjectsByTeacher(Long teacherId) {
        return classSubjectRepository.findByTeacher_UserId(teacherId);
    }
}
