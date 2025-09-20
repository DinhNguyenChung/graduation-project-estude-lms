package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.ClazzWithStudentsDTO;
import org.example.estudebackendspring.dto.StudentDTO;
import org.example.estudebackendspring.dto.SubjectDTO;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final ClassSubjectRepository classSubjectRepository;

    public TeacherService(TeacherRepository teacherRepository, ClassSubjectRepository classSubjectRepository) {
        this.teacherRepository = teacherRepository;
        this.classSubjectRepository = classSubjectRepository;
    }

    public Optional<Teacher> getTeacherById(Long teacherId) {
        return Optional.ofNullable(teacherRepository.findByUserId(teacherId));
    }

    // ds học sinh chủ nhiệm
    public List<ClazzWithStudentsDTO> getHomeroomStudents(Long teacherId) {
        Teacher teacher = teacherRepository.findByUserId(teacherId);
        if (teacher == null) {
            return Collections.emptyList();
        }

        return teacher.getHomeroomClasses().stream().map(clazz -> {
            List<StudentDTO> students = clazz.getEnrollments().stream()
                    .map(e -> {
                        Student s = e.getStudent();
                        return new StudentDTO(
                                s.getUserId(),
                                s.getStudentCode(),
                                s.getFullName(),
                                s.getEmail(),
                                s.getNumberPhone()
                        );
                    })
                    .collect(Collectors.toList());

            return new ClazzWithStudentsDTO(
                    clazz.getClassId(),
                    clazz.getName(),
                    clazz.getGradeLevel().name(),
                    clazz.getClassSize(),
                    clazz.getTerms(),
                    students
            );
        }).collect(Collectors.toList());
    }


    // ds học sinh theo môn học
    public List<Student> getStudentsBySubject(Long teacherId, Long subjectId) {
        Teacher teacher = teacherRepository.findByUserId(teacherId);
        if (teacher == null) return List.of();

        return teacher.getClassSubjects().stream()
                .filter(cs -> cs.getSubject().getSubjectId().equals(subjectId))
                .flatMap(cs -> cs.getTerm().getClazz().getEnrollments().stream()
                        .map(e -> e.getStudent()))
                .distinct()
                .collect(Collectors.toList());
    }
    public List<SubjectDTO> getSubjectsByTeacher(Long teacherId) {
        List<Subject> subjects = classSubjectRepository.findSubjectsByTeacherId(teacherId);
        return subjects.stream()
                .map(s -> new SubjectDTO(s.getSubjectId(), s.getName(), s.getDescription()))
                .collect(Collectors.toList());
    }
    public List<StudentDTO> getStudentsByTeacherAndSubject(Long teacherId, Long subjectId) {
        List<Student> students = classSubjectRepository.findStudentsByTeacherAndSubject(teacherId, subjectId);
        return students.stream()
                .map(s -> new StudentDTO(
                        s.getUserId(),
                        s.getStudentCode(),
                        s.getFullName(),
                        s.getEmail(),
                        s.getNumberPhone()
                ))
                .collect(Collectors.toList());
    }
}
