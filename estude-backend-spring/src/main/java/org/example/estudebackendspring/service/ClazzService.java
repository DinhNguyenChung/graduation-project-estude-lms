package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateClazzRequest;
import org.example.estudebackendspring.dto.UpdateClazzRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.springframework.stereotype.Service;

@Service
public class ClazzService {
    private final ClazzRepository clazzRepository;
    public ClazzService(ClazzRepository clazzRepository) {
        this.clazzRepository = clazzRepository;
    }
    public Clazz createClazz(CreateClazzRequest req) {
        // kiểm tra trùng (ví dụ: name + term unique)
        if (req.getTerm() == null) {
            if (clazzRepository.existsByNameAndTerm(req.getName(), null)) {
                throw new DuplicateResourceException("Class with same name and term already exists");
            }
        } else {
            if (clazzRepository.existsByNameAndTerm(req.getName(), req.getTerm())) {
                throw new DuplicateResourceException("Class with same name and term already exists");
            }
        }

        Clazz c = new Clazz();
        c.setName(req.getName());
        c.setTerm(req.getTerm());
        c.setClassSize(req.getClassSize());
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

        // nếu đổi name+term, kiểm tra duplicate
        if (!c.getName().equals(req.getName()) || (c.getTerm() == null ? req.getTerm() != null : !c.getTerm().equals(req.getTerm()))) {
            if (clazzRepository.existsByNameAndTerm(req.getName(), req.getTerm())) {
                throw new DuplicateResourceException("Another class with same name and term already exists");
            }
        }

        c.setName(req.getName());
        c.setTerm(req.getTerm());
        c.setClassSize(req.getClassSize());
        return clazzRepository.save(c);
    }

    public void deleteClazz(Long classId) {
        if (!clazzRepository.existsById(classId)) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }
        clazzRepository.deleteById(classId);
    }
}
