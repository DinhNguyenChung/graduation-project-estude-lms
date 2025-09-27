package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.ClassStatisticsDTO;
import org.example.estudebackendspring.dto.StudentStatisticsDTO;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AttendanceStatus;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SubjectGradeRepository subjectGradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClazzRepository classRepository;

    // Thống kê cho 1 học sinh
    @Transactional
    public StudentStatisticsDTO getStudentStatistics(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // 1. Lấy enrollment hiện tại (lấy lớp mới nhất nếu học sinh có nhiều enrollment)
//        Enrollment enrollment = enrollmentRepository.findByStudent(student)
//                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for student"));
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        if (enrollments.isEmpty()) {
            throw new ResourceNotFoundException("Enrollment not found for student");
        }

    // Lấy enrollment mới nhất theo dateJoined
        Enrollment enrollment = enrollments.stream()
                    .max(Comparator.comparing(Enrollment::getDateJoined))
                    .orElseThrow(() -> new ResourceNotFoundException("No enrollment found"));

        Clazz clazz = enrollment.getClazz();

        if (clazz == null) {
            throw new ResourceNotFoundException("Student has no class assigned");
        }

        // 2. Lấy kỳ hiện tại của lớp (giả sử lấy kỳ gần nhất theo ngày hiện tại)
        Date today = new Date();
        Term currentTerm = clazz.getTerms().stream()
                .filter(term -> !term.getBeginDate().after(today) && !term.getEndDate().before(today))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No current term found for class"));

        // 3. Lấy danh sách môn học hiện tại của kỳ
        List<ClassSubject> currentSubjects = currentTerm.getClassSubjects();

        // 4. Lấy điểm của học sinh theo môn hiện tại
        List<SubjectGrade> grades = currentSubjects.stream()
                .map(cs -> subjectGradeRepository.findByStudentAndClassSubject(student, cs))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. Kiểm tra tất cả môn đều có actualAverage
//        boolean allSubjectsGraded = grades.stream()
//                .allMatch(g -> g.getActualAverage() != null);
//
//        Double avg = null;
//        if (allSubjectsGraded && !grades.isEmpty()) {
//            avg = grades.stream()
//                    .mapToDouble(g -> g.getActualAverage())
//                    .average()
//                    .orElse(0.0);
//        }
        // 5. Tính điểm trung bình (có môn nào tính môn đó)
        List<Float> availableGrades = grades.stream()
                .map(SubjectGrade::getActualAverage)
                .filter(Objects::nonNull)
                .toList();

        Double avg = null;
        if (!availableGrades.isEmpty()) {
            avg = availableGrades.stream()
                    .mapToDouble(Float::doubleValue)  // chuyển Float sang double khi tính
                    .average()
                    .orElse(0.0);
        }



        // 6. Xếp hạng học sinh trong lớp theo điểm trung bình
        List<Student> allStudents = enrollmentRepository.findByClazz(clazz).stream()
                .map(Enrollment::getStudent)
                .toList();

        // Tính điểm trung bình cho từng học sinh
        Map<Long, Double> studentAvgMap = new HashMap<>();
        for (Student s : allStudents) {
            List<SubjectGrade> sGrades = subjectGradeRepository.findByStudent(s).stream()
                    .filter(g -> currentSubjects.contains(g.getClassSubject()))
                    .filter(g -> g.getActualAverage() != null)
                    .toList();
            if (sGrades.size() == currentSubjects.size()) {
                double sAvg = sGrades.stream().mapToDouble(SubjectGrade::getActualAverage).average().orElse(0.0);
                studentAvgMap.put(s.getUserId(), sAvg);
            } else {
                studentAvgMap.put(s.getUserId(), null);
            }
        }

        // Sắp xếp để tính thứ hạng
        List<Double> sortedAvgs = studentAvgMap.values().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .toList();

        Integer rank = null;
        if (avg != null) {
            rank = sortedAvgs.indexOf(avg) + 1;
        }

        // 7. Số môn học hiện tại và số môn đã hoàn thành (actualAverage >= 5)
        int totalSubjects = currentSubjects.size();
        long completedSubjects = grades.stream()
                .filter(g -> g.getActualAverage() != null && g.getActualAverage() >= 3.5)
                .count();

        // 8. Thống kê Assignment / Submission
        long totalAssignments = assignmentRepository.countByStudentAndTerm(studentId, currentTerm.getTermId());
        long submitted = submissionRepository.countByStudentAndTerm(studentId, currentTerm.getTermId());
        long late = submissionRepository.countLateByStudentAndTerm(studentId, currentTerm.getTermId());
        double submissionRate = totalAssignments == 0 ? 0 : (double) submitted / totalAssignments * 100;
        double lateRate = totalAssignments == 0 ? 0 : (double) late / totalAssignments * 100;

        // 9. Thống kê Attendance
        long totalSessions = attendanceRecordRepository.countByStudentAndTerm(studentId, currentTerm.getTermId());
        long absentSessions = attendanceRecordRepository.countAbsentByStudentAndTerm(studentId, currentTerm.getTermId());
        double attendanceRate = totalSessions == 0 ? 0 : ((double)(totalSessions - absentSessions) / totalSessions) * 100;

        return new StudentStatisticsDTO(
                studentId,
                student.getFullName(),
                avg,
                rank,
                allStudents.size(),
                totalSubjects,
                (int) completedSubjects,
                submissionRate,
                lateRate,
                attendanceRate,
                totalSessions,
                absentSessions
        );
    }

    // Thống kê cho cả lớp (Teacher dùng)
    public ClassStatisticsDTO getClassStatistics(Long teacherId, Long classId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Clazz clazz = classRepository.findById(classId).orElseThrow(() -> new ResourceNotFoundException("Clazz not found"));

        // Giả sử Clazz có danh sách enrollments → students
        List<Student> students = studentRepository.findStudentsByClassId(classId);

        List<StudentStatisticsDTO> studentStats = students.stream()
                .map(s -> getStudentStatistics(s.getUserId()))
                .toList();

        Double classAvg = studentStats.stream()
                .map(StudentStatisticsDTO::getAverageScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Double submissionRate = studentStats.stream()
                .mapToDouble(StudentStatisticsDTO::getSubmissionRate)
                .average().orElse(0.0);

        Double attendanceRate = studentStats.stream()
                .mapToDouble(StudentStatisticsDTO::getAttendanceRate)
                .average().orElse(0.0);

        return new ClassStatisticsDTO(
                classId,
                clazz.getName(),
                classAvg,
                submissionRate,
                attendanceRate,
                studentStats
        );
    }
}

