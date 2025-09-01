package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.springframework.stereotype.Service;

@Service
public class HomeroomService {
    private final ClazzRepository clazzRepository;
    private final TeacherRepository teacherRepository;

    public HomeroomService(ClazzRepository clazzRepository, TeacherRepository teacherRepository) {
        this.clazzRepository = clazzRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public Clazz assignHomeroomTeacher(Long classId, Long teacherId) {
        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (!teacher.isAdmin() && !teacher.isHomeroomTeacher()) {
            throw new RuntimeException("Teacher is not allowed to be a homeroom teacher");
        }

        if (clazz.getHomeroomTeacher() != null) {
            throw new RuntimeException("This class already has a homeroom teacher");
        }

        if (teacher.getHomeroomClass() != null) {
            throw new RuntimeException("This teacher is already assigned to another class");
        }

        clazz.setHomeroomTeacher(teacher);
        teacher.setHomeroomClass(clazz);

        return clazzRepository.save(clazz);
    }
}