package org.example.estudebackendspring;

import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.UserRole;
import org.example.estudebackendspring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Date;

/**
 * Test Data Loader - Creates test data programmatically
 * This runs after Spring context loads and creates all necessary test data
 */
@Component
public class TestDataLoader {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void loadTestData() {
        try {
            System.out.println("🔧 Starting test data loader...");
            
            // Check if data already exists
            long studentCount = studentRepository.count();
            System.out.println("📊 Current student count: " + studentCount);
            
            if (studentCount > 0) {
                System.out.println("✓ Test data already loaded");
                return;
            }

            System.out.println("🔧 Loading test data...");

            // 1. Create School
            School school = new School();
            school.setSchoolCode("SCHOOL_TEST_001");
            school.setSchoolName("Test School");
            school.setAddress("123 Test Street");
            school.setContactEmail("school@test.edu");
            school.setContactPhone("0123456789");
            school = schoolRepository.save(school);
            System.out.println("✓ School created: ID=" + school.getSchoolId() + ", Code=" + school.getSchoolCode());

            // 2. Create Admin User
            Admin admin = new Admin();
            admin.setEmail("admin01@test.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setFullName("Admin Test");
            admin.setNumberPhone("0123456789");
            admin.setSchool(school);
            admin.setAdminCode("admin01");
            admin = adminRepository.save(admin);
            System.out.println("✓ Admin created: ID=" + admin.getUserId() + ", Code=" + admin.getAdminCode());

            // 3. Create Teacher User
            Teacher teacher = new Teacher();
            teacher.setEmail("teacher01@test.edu");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setRole(UserRole.TEACHER);
            teacher.setFullName("Giáo Viên Test");
            teacher.setNumberPhone("0123456790");
            teacher.setSchool(school);
            teacher.setTeacherCode("teacher01");
            teacher.setAdmin(false);
            teacher.setHomeroomTeacher(false);
            teacher = teacherRepository.save(teacher);
            System.out.println("✓ Teacher created: ID=" + teacher.getUserId() + ", Code=" + teacher.getTeacherCode());

            // 4. Create Student User
            Student student = new Student();
            student.setEmail("student1@test.edu");
            student.setPassword(passwordEncoder.encode("123456"));
            student.setRole(UserRole.STUDENT);
            student.setFullName("Học Sinh Test");
            student.setNumberPhone("0123456791");
            student.setSchool(school);
            student.setStudentCode("student1");
            student.setEnrollmentDate(new Date());
            student = studentRepository.save(student);
            System.out.println("✓ Student created: ID=" + student.getUserId() + ", Code=" + student.getStudentCode());

            System.out.println("✅ Test data loaded successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error loading test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
