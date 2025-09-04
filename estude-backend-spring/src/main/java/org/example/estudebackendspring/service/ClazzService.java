package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateClazzRequest;
import org.example.estudebackendspring.dto.UpdateClazzRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClazzService {
    private final ClazzRepository clazzRepository;
    private final SchoolRepository schoolRepository;

    public ClazzService(ClazzRepository clazzRepository, SchoolRepository schoolRepository) {
        this.clazzRepository = clazzRepository;
        this.schoolRepository = schoolRepository;
    }
    public Clazz createClazz(CreateClazzRequest req) {
        // kiểm tra duplicate
        if (clazzRepository.existsByNameAndTerm(req.getName(), req.getTerm())) {
            throw new DuplicateResourceException("Class with same name and term already exists");
        }
        // validate ngày
        if (req.getBeginDate() != null && req.getEndDate() != null
                && req.getBeginDate().after(req.getEndDate())) {
            throw new IllegalArgumentException("Begin date must be before end date");
        }
        // tìm school
        School school = schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + req.getSchoolId()));

        Clazz c = new Clazz();
        c.setName(req.getName());
        c.setTerm(req.getTerm());
        c.setBeginDate(req.getBeginDate());
        c.setEndDate(req.getEndDate());
        c.setClassSize(req.getClassSize());
        c.setSchool(school); // gán school
        return clazzRepository.save(c);
    }

    public Clazz getClazz(Long classId) {
        return clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
    }

    @Transactional
    public Clazz updateClazz(Long classId, UpdateClazzRequest req) {
        Clazz c = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!c.getName().equals(req.getName()) ||
                (c.getTerm() == null ? req.getTerm() != null : !c.getTerm().equals(req.getTerm()))) {
            if (clazzRepository.existsByNameAndTerm(req.getName(), req.getTerm())) {
                throw new DuplicateResourceException("Another class with same name and term already exists");
            }
        }

        School school = schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + req.getSchoolId()));

        c.setName(req.getName());
        c.setTerm(req.getTerm());
        c.setClassSize(req.getClassSize());
        c.setSchool(school); // update school
        return clazzRepository.save(c);
    }

    public void deleteClazz(Long classId) {
        if (!clazzRepository.existsById(classId)) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }
        clazzRepository.deleteById(classId);
    }
    public List<Clazz> getClassesBySchool(Long schoolId) {
        return clazzRepository.findBySchool_SchoolId(schoolId);
    }
}
