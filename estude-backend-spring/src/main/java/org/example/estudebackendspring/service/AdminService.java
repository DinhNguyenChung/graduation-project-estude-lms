package org.example.estudebackendspring.service;

import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.enums.UserRole;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AdminService {
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(SchoolRepository schoolRepository,
                        StudentRepository studentRepository,
                        TeacherRepository teacherRepository,
                        PasswordEncoder passwordEncoder) {
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    public Student createStudent(Long schoolId, String studentCode, String fullName,
//                                 String email, String phone, String password, Date dob) {
//        School school = schoolRepository.findById(schoolId)
//                .orElseThrow(() -> new RuntimeException("School not found"));
//        Student student = new Student();
//        student.setStudentCode(studentCode);
//        student.setFullName(fullName);
//        student.setEmail(email);
//        student.setNumberPhone(phone);
//        student.setPassword(passwordEncoder.encode(password));
//        student.setDob(dob);
//        student.setEnrollmentDate(new Date());
//        student.setRole(UserRole.STUDENT);
//        student.setSchool(school);
//        return studentRepository.save(student);
//    }
public Student createStudent(Long schoolId, String studentCode, String fullName,
                             String email, String phone, String password, Date dob) {
    School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy trường học"));

    // Kiểm tra trùng dữ liệu
    if (studentRepository.existsByStudentCode(studentCode)) {
        throw new DuplicateResourceException("Mã sinh viên đã tồn tại: " + studentCode);
    }
    if (studentRepository.existsByEmail(email)) {
        throw new DuplicateResourceException("Email đã tồn tại: " + email);
    }
    if (studentRepository.existsByNumberPhone(phone)) {
        throw new DuplicateResourceException("Số điện thoại đã tồn tại: " + phone);
    }

    Student student = new Student();
    student.setStudentCode(studentCode);
    student.setFullName(fullName);
    student.setEmail(email);
    student.setNumberPhone(phone);
    student.setPassword(passwordEncoder.encode(password));
    student.setDob(dob);
    student.setEnrollmentDate(new Date());
    student.setRole(UserRole.STUDENT);
    student.setSchool(school);
    return studentRepository.save(student);
}

//    public Teacher createTeacher(Long schoolId, String teacherCode, String fullName,
//                                 String email, String phone, String password, Date dob,
//                                 boolean isAdmin, boolean isHomeroomTeacher) {
//        School school = schoolRepository.findById(schoolId)
//                .orElseThrow(() -> new RuntimeException("School not found"));
//        Teacher teacher = new Teacher();
//        teacher.setTeacherCode(teacherCode);
//        teacher.setFullName(fullName);
//        teacher.setEmail(email);
//        teacher.setNumberPhone(phone);
//        teacher.setPassword(passwordEncoder.encode(password));
//        teacher.setDob(dob);
//        teacher.setHireDate(new Date());
//        teacher.setRole(UserRole.TEACHER);
//        teacher.setSchool(school);
//        teacher.setAdmin(isAdmin);
//        teacher.setHomeroomTeacher(isHomeroomTeacher);
//        return teacherRepository.save(teacher);
//    }
public Teacher createTeacher(Long schoolId, String teacherCode, String fullName,
                             String email, String phone, String password, Date dob,
                             boolean isAdmin, boolean isHomeroomTeacher) {
    School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy trường học"));

    if (teacherRepository.existsByTeacherCode(teacherCode)) {
        throw new DuplicateResourceException("Mã giáo viên đã tồn tại: " + teacherCode);
    }
    if (teacherRepository.existsByEmail(email)) {
        throw new DuplicateResourceException("Email đã tồn tại: " + email);
    }
    if (teacherRepository.existsByNumberPhone(phone)) {
        throw new DuplicateResourceException("Số điện thoại đã tồn tại: " + phone);
    }

    Teacher teacher = new Teacher();
    teacher.setTeacherCode(teacherCode);
    teacher.setFullName(fullName);
    teacher.setEmail(email);
    teacher.setNumberPhone(phone);
    teacher.setPassword(passwordEncoder.encode(password));
    teacher.setDob(dob);
    teacher.setHireDate(new Date());
    teacher.setRole(UserRole.TEACHER);
    teacher.setSchool(school);
    teacher.setAdmin(isAdmin);
    teacher.setHomeroomTeacher(isHomeroomTeacher);
    return teacherRepository.save(teacher);
}

}
