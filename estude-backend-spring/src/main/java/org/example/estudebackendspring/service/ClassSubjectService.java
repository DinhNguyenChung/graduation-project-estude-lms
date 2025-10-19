package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.ClassSubjectResponse;
import org.example.estudebackendspring.dto.CreateClassSubjectRequest;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public ClassSubjectResponse getClassSubjectByClassSubjectId(Long classSubjectId) {
        ClassSubject cs = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy ClassSubject với id: " + classSubjectId));

        // Lấy lớp từ term
        Clazz clazz = cs.getTerm().getClazz();

        return ClassSubjectResponse.builder()
                .classSubjectId(cs.getClassSubjectId())
                .subjectId(cs.getSubject() != null ? cs.getSubject().getSubjectId() : null)
                .subjectName(cs.getSubject() != null ? cs.getSubject().getName() : null)
                .teacherId(cs.getTeacher() != null ? cs.getTeacher().getUserId() : null)
                .teacherName(cs.getTeacher() != null ? cs.getTeacher().getFullName() : null)
                .termName(cs.getTerm() != null ? cs.getTerm().getName() : null)
                .classId(clazz != null ? clazz.getClassId() : null)
                .className(clazz != null ? clazz.getName() : null)
                .gradeLevel(clazz != null && clazz.getGradeLevel() != null ? clazz.getGradeLevel().name() : null)
                .build();
    }

    @Transactional
//    public List<ClassSubject> assignSubjectToClass(CreateClassSubjectRequest req) {
//        Clazz clazz = clazzRepository.findById(req.getClassId())
//                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + req.getClassId()));
//
//        Subject subject = subjectRepository.findById(req.getSubjectId())
//                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + req.getSubjectId()));
//
//        Teacher teacher = null;
//        if (req.getTeacherId() != null) {
//            teacher = teacherRepository.findById(req.getTeacherId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + req.getTeacherId()));
//        }
//
//        List<ClassSubject> createdList = new ArrayList<>();
//
//        for (Long termId : req.getTermIds()) {
//            Term term = termRepository.findById(termId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termId));
//
//            // Kiểm tra term có thuộc class hay không
//            if (!term.getClazz().getClassId().equals(req.getClassId())) {
//                throw new IllegalArgumentException("Term with id " + termId + " does not belong to class " + req.getClassId());
//            }
//
//            // Kiểm tra duplicate
//            if (classSubjectRepository.existsByTermAndSubject(term, subject)) {
//                throw new DuplicateResourceException("This subject is already assigned to term " + termId);
//            }
//
//            // Tạo mới ClassSubject
//            ClassSubject cs = new ClassSubject();
//            cs.setTerm(term);
//            cs.setSubject(subject);
//            if (teacher != null) {
//                cs.setTeacher(teacher);
//            }
//
//            createdList.add(classSubjectRepository.save(cs));
//        }
//
//        return createdList;
//    }
    public List<ClassSubject> assignSubjectToClass(CreateClassSubjectRequest req) {
        // Tìm Clazz
        Clazz clazz = clazzRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + req.getClassId()));

        // Tìm Subject
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + req.getSubjectId()));

        // Tìm Teacher (nếu có)
        Teacher teacher = null;
        if (req.getTeacherId() != null) {
            teacher = teacherRepository.findById(req.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + req.getTeacherId()));
        }

        // Kiểm tra termIds không trùng lặp
        Set<Long> termIdsSet = new HashSet<>(req.getTermIds());
        if (termIdsSet.size() != req.getTermIds().size()) {
            throw new IllegalArgumentException("Duplicate term IDs are not allowed");
        }

        // Nếu classSubjectId được cung cấp, chỉ nên có một termId (vì mỗi ClassSubject liên kết với một Term)
        if (req.getClassSubjectId() != null && req.getTermIds().size() > 1) {
            throw new IllegalArgumentException("Khi cập nhật ClassSubject, chỉ nên cung cấp một termId");
        }

        List<ClassSubject> createdOrUpdatedList = new ArrayList<>();

        if (req.getClassSubjectId() != null) {
            // Cập nhật ClassSubject hiện có
            ClassSubject cs = classSubjectRepository.findById(req.getClassSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found with id: " + req.getClassSubjectId()));

            // Kiểm tra ClassSubject có khớp với termId và subjectId
            Long termId = req.getTermIds().get(0);
            Term term = termRepository.findById(termId)
                    .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termId));

            if (!term.getClazz().getClassId().equals(req.getClassId())) {
                throw new IllegalArgumentException("Term with id " + termId + " does not belong to class " + req.getClassId());
            }

            if (!cs.getTerm().getTermId().equals(termId) || !cs.getSubject().getSubjectId().equals(req.getSubjectId())) {
                throw new IllegalArgumentException("ClassSubject with id " + req.getClassSubjectId() + " does not match term or subject");
            }

            // Cập nhật Teacher
            cs.setTeacher(teacher);

            // Lưu ClassSubject
            createdOrUpdatedList.add(classSubjectRepository.save(cs));
        } else {
            // Tạo mới ClassSubject cho mỗi termId
            for (Long termId : req.getTermIds()) {
                Term term = termRepository.findById(termId)
                        .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termId));

                // Kiểm tra Term có thuộc Class
                if (!term.getClazz().getClassId().equals(req.getClassId())) {
                    throw new IllegalArgumentException("Term with id " + termId + " does not belong to class " + req.getClassId());
                }

                // Kiểm tra trùng lặp
                if (classSubjectRepository.existsByTermAndSubject(term, subject)) {
                    throw new DuplicateResourceException("Subject is already assigned to term " + termId);
                }

                // Tạo mới ClassSubject
                ClassSubject cs = new ClassSubject();
                cs.setTerm(term);
                cs.setSubject(subject);
                cs.setTeacher(teacher);

                // Lưu ClassSubject
                createdOrUpdatedList.add(classSubjectRepository.save(cs));
            }
        }

        return createdOrUpdatedList;
    }


    public void removeClassSubject(Long classSubjectId) {
        ClassSubject cs = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found with id: " + classSubjectId));
        classSubjectRepository.delete(cs);
    }

    public ClassSubject getClassSubjectById(Long classSubjectId) {
        return classSubjectRepository.findByClassSubjectId(classSubjectId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found with id: " + classSubjectId));
    }
}
