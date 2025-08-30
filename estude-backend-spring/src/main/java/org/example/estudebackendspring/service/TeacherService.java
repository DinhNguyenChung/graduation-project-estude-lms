package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Optional<Teacher> getTeacherById(Long teacherId) {
        return Optional.ofNullable(teacherRepository.findByUserId(teacherId));
    }

    // ds học sinh chủ nhiệm
    public List<Student> getHomeroomStudents(Long teacherId) {
        Teacher teacher = teacherRepository.findByUserId(teacherId);
        if (teacher == null || !teacher.isHomeroomTeacher()) {
            return List.of();
        }
        // Lấy học sinh từ lớp chủ nhiệm
        return teacher.getClassSubjects()
                .stream()
                .flatMap(cs -> cs.getClazz().getEnrollments().stream()
                        .map(e -> e.getStudent()))
                .distinct()
                .collect(Collectors.toList());
    }

    // ds học sinh theo môn học
    public List<Student> getStudentsBySubject(Long teacherId, Long subjectId) {
        Teacher teacher = teacherRepository.findByUserId(teacherId);
        if (teacher == null) return List.of();

        return teacher.getClassSubjects().stream()
                .filter(cs -> cs.getSubject().getSubjectId().equals(subjectId))
                .flatMap(cs -> cs.getClazz().getEnrollments().stream()
                        .map(e -> e.getStudent()))
                .distinct()
                .collect(Collectors.toList());
    }
}
