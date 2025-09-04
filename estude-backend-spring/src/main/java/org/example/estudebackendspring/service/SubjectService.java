package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateSubjectRequest;
import org.example.estudebackendspring.dto.UpdateSubjectRequest;
import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;
    public SubjectService(SubjectRepository subjectRepository, SchoolRepository schoolRepository) {
        this.subjectRepository = subjectRepository;
        this.schoolRepository = schoolRepository;
    }
    public Subject createSubject(CreateSubjectRequest req) {
        if(subjectRepository.existsByNameAndSchoolsSchoolId(req.getName(), req.getSchoolId())) {
            throw new DuplicateResourceException("Subject name already exists in school: " + req.getName());
        }
        // Tìm trường
        School school = schoolRepository.findBySchoolId(req.getSchoolId());
        if(school == null) {
            throw new ResourceNotFoundException("School not found: " + req.getSchoolId());
        }
        // Tạo môn học mới
        Subject subject = new Subject();
        subject.setName(req.getName());
        subject.setDescription(req.getDescription());
        subject.getSchools().add(school); // Thêm trường vào tập schools

        return subjectRepository.save(subject);
    }

    public Subject getSubject(Long subjectId) {
        return subjectRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
    }

    @Transactional
    public Subject updateSubject(Long subjectId, UpdateSubjectRequest req) {
        Subject s = subjectRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (!s.getName().equals(req.getName()) && subjectRepository.existsByName(req.getName())) {
            throw new DuplicateResourceException("Another subject with same name already exists: " + req.getName());
        }

        s.setName(req.getName());
        s.setDescription(req.getDescription());
        return subjectRepository.save(s);
    }

    public void deleteSubject(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        subjectRepository.deleteById(subjectId);
    }
    public List<Subject> getSubjectsByClassId(Long classId) {
        return subjectRepository.findSubjectsByClassId(classId);
    }
    public List<Subject> getAllSubjectsBySchoolId(Long schoolId) {
        return subjectRepository.findSubjectsBySchoolId(schoolId);
    }
}
