package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateClassSubjectRequest;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;
    private final ClazzRepository clazzRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public ClassSubjectService(ClassSubjectRepository classSubjectRepository, ClazzRepository clazzRepository,
                               SubjectRepository subjectRepository, TeacherRepository teacherRepository) {
        this.classSubjectRepository = classSubjectRepository;
        this.clazzRepository = clazzRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<ClassSubject> getClassSubjectsByTeacher(Long teacherId) {
        return classSubjectRepository.findByTeacher_UserId(teacherId);
    }
    @Transactional
    public ClassSubject assignSubjectToClass(CreateClassSubjectRequest req) {
        Clazz clazz = clazzRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + req.getClassId()));

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + req.getSubjectId()));

        // kiểm tra duplicate
        if (classSubjectRepository.existsByClazzAndSubject(clazz, subject)) {
            throw new DuplicateResourceException("This subject is already assigned to the class");
        }

        ClassSubject cs = new ClassSubject();
        cs.setClazz(clazz);
        cs.setSubject(subject);

        if (req.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(req.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + req.getTeacherId()));
            cs.setTeacher(teacher);
        }

        return classSubjectRepository.save(cs);
    }

    public void removeClassSubject(Long classSubjectId) {
        ClassSubject cs = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found with id: " + classSubjectId));
        classSubjectRepository.delete(cs);
    }
}
