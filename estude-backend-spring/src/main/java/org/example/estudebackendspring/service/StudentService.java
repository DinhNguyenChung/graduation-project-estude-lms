package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.EnrollmentRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassSubjectRepository classSubjectRepository;

    public StudentService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository, ClassSubjectRepository classSubjectRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.classSubjectRepository = classSubjectRepository;
    }

    public Optional<Student> getStudentById(Long studentId) {
        return Optional.ofNullable(studentRepository.findByUserId(studentId));
    }
    public List<Student> getStudentsByClass(Long classId) {
        return studentRepository.findStudentsByClassId(classId);
    }

    public List<Student> getStudentsBySchool(Long schoolId) {
        return studentRepository.findStudentsBySchoolId(schoolId);
    }
    // Lấy danh sách lớp của 1 học sinh
    public List<Clazz> getClassesByStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_UserId(studentId);
        return enrollments.stream()
                .map(Enrollment::getClazz)
                .distinct()
                .toList();
    }

    // Lấy danh sách môn học của học sinh (với EAGER FETCH)
    public List<ClassSubject> getSubjectsByStudent(Long studentId) {
        List<Clazz> classes = getClassesByStudent(studentId);
        return classes.stream()
                .flatMap(c -> classSubjectRepository.findByClassIdWithDetails(c.getClassId()).stream())
                .distinct()
                .toList();
    }
}