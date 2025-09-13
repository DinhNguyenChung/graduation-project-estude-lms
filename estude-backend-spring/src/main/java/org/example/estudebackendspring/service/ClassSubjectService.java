package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateClassSubjectRequest;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;
    private final ClazzRepository clazzRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final TermRepository termRepository;

    public ClassSubjectService(ClassSubjectRepository classSubjectRepository, ClazzRepository clazzRepository,
                               SubjectRepository subjectRepository, TeacherRepository teacherRepository, TermRepository termRepository) {
        this.classSubjectRepository = classSubjectRepository;
        this.clazzRepository = clazzRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.termRepository = termRepository;
    }

    public List<ClassSubject> getClassSubjectsByTeacher(Long teacherId) {
        return classSubjectRepository.findByTeacher_UserId(teacherId);
    }

    @Transactional
    public List<ClassSubject> assignSubjectToClass(CreateClassSubjectRequest req) {
        Clazz clazz = clazzRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + req.getClassId()));

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + req.getSubjectId()));

        Teacher teacher = null;
        if (req.getTeacherId() != null) {
            teacher = teacherRepository.findById(req.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + req.getTeacherId()));
        }

        List<ClassSubject> createdList = new ArrayList<>();

        for (Long termId : req.getTermIds()) {
            Term term = termRepository.findById(termId)
                    .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termId));

            // Kiểm tra term có thuộc class hay không
            if (!term.getClazz().getClassId().equals(req.getClassId())) {
                throw new IllegalArgumentException("Term with id " + termId + " does not belong to class " + req.getClassId());
            }

            // Kiểm tra duplicate
            if (classSubjectRepository.existsByTermAndSubject(term, subject)) {
                throw new DuplicateResourceException("This subject is already assigned to term " + termId);
            }

            // Tạo mới ClassSubject
            ClassSubject cs = new ClassSubject();
            cs.setTerm(term);
            cs.setSubject(subject);
            if (teacher != null) {
                cs.setTeacher(teacher);
            }

            createdList.add(classSubjectRepository.save(cs));
        }

        return createdList;
    }


    public void removeClassSubject(Long classSubjectId) {
        ClassSubject cs = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found with id: " + classSubjectId));
        classSubjectRepository.delete(cs);
    }
}
