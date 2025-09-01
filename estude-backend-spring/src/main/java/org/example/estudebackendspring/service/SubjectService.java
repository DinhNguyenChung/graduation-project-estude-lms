package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateSubjectRequest;
import org.example.estudebackendspring.dto.UpdateSubjectRequest;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }
    public Subject createSubject(CreateSubjectRequest req) {
        if (subjectRepository.existsByName(req.getName())) {
            throw new DuplicateResourceException("Subject name already exists: " + req.getName());
        }
        Subject s = new Subject();
        s.setName(req.getName());
        s.setDescription(req.getDescription());
        return subjectRepository.save(s);
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
}
