package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.CreateEnrollmentRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Term;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.EnrollmentRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List<Enrollment> enrollStudents(Long classId, List<Long> studentIds) {
        Clazz clazz = clazzRepository.findById(classId).orElseThrow();

        // Lấy các term của lớp này
        List<Term> terms = clazz.getTerms();
        if (terms == null || terms.isEmpty()) {
            throw new IllegalArgumentException("Lớp này chưa có kỳ học (Term) nào, không thể ghi danh!");
        }

        List<Enrollment> saved = new ArrayList<>();

        for (Long sid : studentIds) {
            Student student = studentRepository.findById(sid).orElseThrow();

            // Kiểm tra conflict với từng term của lớp
            for (Term term : terms) {
                boolean conflict = enrollmentRepository.existsEnrollmentConflict(
                        student,
                        term.getBeginDate(),
                        term.getEndDate()
                );

                if (conflict) {
                    throw new IllegalArgumentException("Học sinh " + student.getFullName()
                            + " đã tham gia một lớp khác trong khoảng thời gian của kỳ: " + term.getName());
                }
            }

            // Kiểm tra trùng chính lớp này
            if (!enrollmentRepository.existsByClazzAndStudent(clazz, student)) {
                Enrollment e = new Enrollment();
                e.setClazz(clazz);
                e.setStudent(student);
                e.setDateJoined(new Date());
                saved.add(enrollmentRepository.save(e));
            }
        }

        // Cập nhật sĩ số
        clazz.setClassSize(enrollmentRepository.countByClazz(clazz));
        clazzRepository.save(clazz);

        return saved;
    }



    @Transactional
    public void removeEnrollment(Long enrollmentId) {
        Enrollment e = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        Clazz clazz = e.getClazz();
        enrollmentRepository.delete(e);

        // Sau khi xoá -> cập nhật lại classSize
        int count = enrollmentRepository.countByClazz(clazz);
        clazz.setClassSize(count);
        clazzRepository.save(clazz);
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudent_UserId(studentId);
    }

}
