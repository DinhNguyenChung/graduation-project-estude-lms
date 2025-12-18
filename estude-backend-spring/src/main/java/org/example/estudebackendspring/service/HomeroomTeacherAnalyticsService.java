package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.analytics.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Homeroom Teacher Analytics
 * Provides comprehensive class overview across all subjects
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeroomTeacherAnalyticsService {
    
    private final ClazzRepository clazzRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final SubjectGradeRepository subjectGradeRepository;
    
    /**
     * Get comprehensive overview of homeroom class
     * Shows performance across all subjects
     * 
     * @param classId ID of the homeroom class
     * @param teacherId ID of the homeroom teacher (for authorization)
     * @return Complete class overview with all subjects and students
     */
        @Transactional(readOnly = true)
        @Cacheable(value = "homeroomClassOverview", key = "#classId", unless = "#result == null")
    public HomeroomClassDTO getHomeroomClassOverview(Long classId, Long teacherId) {
        log.info("Fetching homeroom class overview for class ID: {} by teacher ID: {}", classId, teacherId);
        
        // 1. Get class and verify homeroom teacher
        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));
        
        log.info("Found class: {}, grade level: {}", clazz.getName(), clazz.getGradeLevel());
        
        // Optional: Verify teacher is homeroom teacher (comment out if not needed)
        // if (clazz.getHomeroomTeacher() == null || !clazz.getHomeroomTeacher().getUserId().equals(teacherId)) {
        //     throw new RuntimeException("Teacher is not homeroom teacher of this class");
        // }
        
        String homeroomTeacherName = "N/A";
        if (clazz.getHomeroomTeacher() != null) {
            homeroomTeacherName = clazz.getHomeroomTeacher().getFullName();
        }
        
        // 2. Get all enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findByClazzClassId(classId);
        int studentCount = enrollments.size();
        log.info("Class has {} enrolled students", studentCount);
        
        // 3. Get all subjects taught in this class (from all terms)
        List<ClassSubject> classSubjects = classSubjectRepository.findByTerm_Clazz_ClassId(classId);
        log.info("Found {} class subjects for class ID: {}", classSubjects.size(), classId);
        
        // 4. Calculate performance for each subject
        List<HomeroomClassDTO.SubjectPerformanceDTO> subjectPerformanceList = new ArrayList<>();
        double totalAvgScore = 0.0;
        int totalPassCount = 0;
        int totalExcellentCount = 0;
        int totalValidGrades = 0;
        
        for (ClassSubject cs : classSubjects) {
            if (cs.getSubject() == null || cs.getTeacher() == null || cs.getTerm() == null) {
                continue;
            }
            
            String subjectName = cs.getSubject().getName();
            String teacherName = cs.getTeacher().getFullName();
            String termName = cs.getTerm().getName();
            
            // Get all grades for this subject
            List<SubjectGrade> grades = subjectGradeRepository.findByClassSubject_ClassSubjectId(cs.getClassSubjectId());
            
            if (grades.isEmpty()) {
                // Subject has no grades yet, still add with 0 values
                subjectPerformanceList.add(HomeroomClassDTO.SubjectPerformanceDTO.builder()
                        .subjectName(subjectName)
                        .termName(termName)
                        .teacherName(teacherName)
                        .avgScore(0.0)
                        .passRate(0.0)
                        .excellentRate(0.0)
                        .build());
                log.info("Subject {} (Term: {}) has no grades yet", subjectName, termName);
                continue;
            }
            
            // Calculate subject metrics
            double subjectAvgScore = 0.0;
            int passCount = 0;
            int excellentCount = 0;
            int validGradeCount = 0;
            
            for (SubjectGrade grade : grades) {
                Float avgScore = grade.getActualAverage();
                if (avgScore != null && avgScore > 0) {
                    subjectAvgScore += avgScore;
                    validGradeCount++;
                    
                    if (avgScore >= 5.0) {
                        passCount++;
                        totalPassCount++;
                    }
                    if (avgScore >= 9.0) {
                        excellentCount++;
                        totalExcellentCount++;
                    }
                }
            }
            
            if (validGradeCount > 0) {
                subjectAvgScore /= validGradeCount;
                totalAvgScore += subjectAvgScore * validGradeCount;
                totalValidGrades += validGradeCount;
            }
            
            double subjectPassRate = validGradeCount > 0 ? 
                    Math.round((double) passCount / validGradeCount * 10000.0) / 100.0 : 0.0;
            double subjectExcellentRate = validGradeCount > 0 ? 
                    Math.round((double) excellentCount / validGradeCount * 10000.0) / 100.0 : 0.0;
            
            subjectPerformanceList.add(HomeroomClassDTO.SubjectPerformanceDTO.builder()
                    .subjectName(subjectName)
                    .termName(termName)
                    .teacherName(teacherName)
                    .avgScore(Math.round(subjectAvgScore * 100.0) / 100.0)
                    .passRate(subjectPassRate)
                    .excellentRate(subjectExcellentRate)
                    .build());
            
            log.info("Subject {} (Term: {}): avgScore={}, passRate={}, excellentRate={}", 
                    subjectName, termName, subjectAvgScore, subjectPassRate, subjectExcellentRate);
        }
        
        // 5. Calculate overall performance
        double overallAvgScore = totalValidGrades > 0 ? 
                Math.round((totalAvgScore / totalValidGrades) * 100.0) / 100.0 : 0.0;
        double overallPassRate = totalValidGrades > 0 ? 
                Math.round((double) totalPassCount / totalValidGrades * 10000.0) / 100.0 : 0.0;
        double overallExcellentRate = totalValidGrades > 0 ? 
                Math.round((double) totalExcellentCount / totalValidGrades * 10000.0) / 100.0 : 0.0;
        
        OverallPerformanceDTO overallPerformance = OverallPerformanceDTO.builder()
                .avgScore(overallAvgScore)
                .passRate(overallPassRate)
                .excellentRate(overallExcellentRate)
                .comparisonToSchool(OverallPerformanceDTO.ComparisonDTO.builder()
                        .avgScoreDiff(0.0)  // TODO: Compare with school average
                        .passRateDiff(0.0)
                        .excellentRateDiff(0.0)
                        .build())
                .build();
        
        // 6. Calculate student averages across all subjects to find top/at-risk
        Map<Long, List<Float>> studentGrades = new HashMap<>();
        
        for (ClassSubject cs : classSubjects) {
            List<SubjectGrade> grades = subjectGradeRepository.findByClassSubject_ClassSubjectId(cs.getClassSubjectId());
            for (SubjectGrade grade : grades) {
                if (grade.getStudent() != null && grade.getActualAverage() != null && grade.getActualAverage() > 0) {
                    Long studentId = grade.getStudent().getUserId();
                    studentGrades.computeIfAbsent(studentId, k -> new ArrayList<>()).add(grade.getActualAverage());
                }
            }
        }
        
        // Calculate each student's overall average
        List<HomeroomClassDTO.StudentRankDTO> allStudentRanks = new ArrayList<>();
        
        for (Map.Entry<Long, List<Float>> entry : studentGrades.entrySet()) {
            Long studentId = entry.getKey();
            List<Float> grades = entry.getValue();
            
            // Find student info
            Optional<Enrollment> enrollmentOpt = enrollments.stream()
                    .filter(e -> e.getStudent() != null && e.getStudent().getUserId().equals(studentId))
                    .findFirst();
            
            if (enrollmentOpt.isEmpty()) continue;
            
            Student student = enrollmentOpt.get().getStudent();
            
            double studentAvg = grades.stream()
                    .mapToDouble(Float::doubleValue)
                    .average()
                    .orElse(0.0);
            
            allStudentRanks.add(HomeroomClassDTO.StudentRankDTO.builder()
                    .studentId(studentId)
                    .studentName(student.getFullName())
                    .studentCode(student.getStudentCode())
                    .overallScore(Math.round(studentAvg * 100.0) / 100.0)
                    .build());
        }
        
        // Sort by score descending
        allStudentRanks.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        
        // Assign ranks
        for (int i = 0; i < allStudentRanks.size(); i++) {
            allStudentRanks.get(i).setRank(i + 1);
        }
        
        // 7. Get top 5 performers
        List<HomeroomClassDTO.StudentRankDTO> topPerformers = allStudentRanks.stream()
                .limit(5)
                .collect(Collectors.toList());
        
        // 8. Get at-risk students (avg < 5.0)
        List<HomeroomClassDTO.StudentRankDTO> atRiskStudents = allStudentRanks.stream()
                .filter(s -> s.getOverallScore() < 5.0)
                .collect(Collectors.toList());
        
        log.info("Top performers: {}, At-risk students: {}", topPerformers.size(), atRiskStudents.size());
        
        return HomeroomClassDTO.builder()
                .classId(clazz.getClassId())
                .className(clazz.getName())
                .gradeLevel(clazz.getGradeLevel() != null ? clazz.getGradeLevel().toString() : "N/A")
                .homeroomTeacher(homeroomTeacherName)
                .studentCount(studentCount)
                .overallPerformance(overallPerformance)
                .subjectPerformance(subjectPerformanceList)
                .topPerformers(topPerformers)
                .atRiskStudents(atRiskStudents)
                .build();
    }
    
    /**
     * Get detailed performance of a student across all subjects
     * For homeroom teacher to see complete picture
     * 
     * @param studentId ID of the student
     * @param teacherId ID of the homeroom teacher (for authorization)
     * @return Student performance across all subjects
     */
    public StudentPerformanceDTO getStudentCompletePerformance(Long studentId, Long teacherId) {
        log.info("Fetching complete performance for student ID: {} by homeroom teacher ID: {}", 
                studentId, teacherId);
        
        // TODO: Implement:
        // 1. Verify student is in teacher's homeroom class
        // 2. Get all subjects the student is taking
        // 3. Calculate performance in each subject
        // 4. Identify weak subjects (score < 5.0)
        // 5. Identify strong subjects (score >= 9.0)
        // 6. Determine overall progress trend
        
        return StudentPerformanceDTO.builder()
                .studentId(studentId)
                .studentName("N/A")
                .studentCode("N/A")
                .overallScore(0.0)
                .topicScores(Collections.emptyList())
                .weakTopics(Collections.emptyList())
                .strongTopics(Collections.emptyList())
                .progressTrend("STABLE")
                .build();
    }
}
