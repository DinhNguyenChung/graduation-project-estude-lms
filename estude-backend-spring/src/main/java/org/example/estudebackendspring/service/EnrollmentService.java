package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateEnrollmentRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.EnrollmentRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final ClazzRepository clazzRepository;
    private final StudentRepository studentRepository;
    public EnrollmentService(EnrollmentRepository enrollmentRepository, ClazzRepository clazzRepository,
                             StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.clazzRepository = clazzRepository;
        this.studentRepository = studentRepository;
    }


    @Transactional
    public Enrollment enrollStudent(CreateEnrollmentRequest req) {
        Clazz clazz = clazzRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + req.getClassId()));

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + req.getStudentId()));

        if (enrollmentRepository.existsByClazzAndStudent(clazz, student)) {
            throw new DuplicateResourceException("Student is already enrolled in this class");
        }

        Enrollment e = new Enrollment();
        e.setClazz(clazz);
        e.setStudent(student);
        e.setDateJoined(new Date());
        // Lưu enrollment mới
        Enrollment saved = enrollmentRepository.saveAndFlush(e);
        // Đếm lại số học sinh trong lớp
        int count = enrollmentRepository.countByClazz(clazz);
        System.out.println("SL hoc sinh trong lop" +count);
        clazz.setClassSize(count);
        clazzRepository.save(clazz);

//       return enrollmentRepository.save(e);
        return saved;
//        Enrollment e = new Enrollment();
//        e.setClazz(clazz);
//        e.setStudent(student);
//        e.setDateJoined(new Date());
//
//        // Thêm enrollment vào class (để Hibernate quản lý 2 chiều)
//        clazz.getEnrollments().add(e);
//
//        // Cập nhật classSize trực tiếp từ danh sách enrollments
//        clazz.setClassSize(clazz.getEnrollments().size());
//
//        // Lưu class, enrollment sẽ được cascade save
//        clazzRepository.save(clazz);
//        return e;
    }

    public void removeEnrollment(Long enrollmentId) {
        Enrollment e = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
        enrollmentRepository.delete(e);
    }
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudent_UserId(studentId);
    }

}
