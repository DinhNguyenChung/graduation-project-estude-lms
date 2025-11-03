package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.analytics.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Subject Teacher Analytics
 * Provides performance insights for teachers about their classes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAnalyticsService {
    
    private final TeacherRepository teacherRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final SubjectGradeRepository subjectGradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    /**
     * Get overview of all classes taught by a teacher
     * @param teacherId ID of the teacher
     * @return Overview with all classes and overall performance
     */
    @Cacheable(value = "teacherOverview", key = "#teacherId", unless = "#result == null")
    public TeacherClassOverviewDTO getTeacherOverview(Long teacherId) {
        log.info("Fetching overview for teacher ID: {}", teacherId);
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + teacherId));
        
        log.info("Found teacher: userId={}, fullName={}", teacher.getUserId(), teacher.getFullName());
        
        // 1. Get all ClassSubjects where this teacher teaches - with EAGER FETCH
        List<ClassSubject> classSubjects = classSubjectRepository.findByTeacherUserIdWithTermAndClass(teacherId);
        
        log.info("Found {} class subjects for teacher ID: {}", classSubjects.size(), teacherId);
        
        if (classSubjects.isEmpty()) {
            log.warn("No class subjects found for teacher ID: {}", teacherId);
            log.warn("This could mean:");
            log.warn("1. Teacher has not been assigned to any class subjects");
            log.warn("2. Check if data exists in 'class_subjects' table for teacher_id={}", teacherId);
            
            TeacherClassOverviewDTO.TeacherInfo teacherInfo = TeacherClassOverviewDTO.TeacherInfo.builder()
                    .teacherId(teacher.getUserId())
                    .teacherName(teacher.getFullName())
                    .subject("N/A")
                    .totalStudents(0)
                    .build();
            
            OverallPerformanceDTO overallPerformance = OverallPerformanceDTO.builder()
                    .avgScore(0.0)
                    .passRate(0.0)
                    .excellentRate(0.0)
                    .comparisonToSchool(OverallPerformanceDTO.ComparisonDTO.builder()
                            .avgScoreDiff(0.0)
                            .passRateDiff(0.0)
                            .excellentRateDiff(0.0)
                            .build())
                    .build();
            
            return TeacherClassOverviewDTO.builder()
                    .teacherInfo(teacherInfo)
                    .overallPerformance(overallPerformance)
                    .classes(Collections.emptyList())
                    .build();
        }
        
        // 2. Get the main subject the teacher teaches (most frequent)
        Map<String, Long> subjectCounts = classSubjects.stream()
                .filter(cs -> cs.getSubject() != null)
                .collect(Collectors.groupingBy(
                        cs -> cs.getSubject().getName(),
                        Collectors.counting()
                ));
        
        String mainSubject = subjectCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        log.info("Main subject for teacher: {}", mainSubject);
        
        // 3. Calculate total unique students across all classes (from Enrollments, not grades)
        Set<Long> uniqueStudentIds = new HashSet<>();
        for (ClassSubject cs : classSubjects) {
            if (cs.getTerm() != null && cs.getTerm().getClazz() != null) {
                Long classId = cs.getTerm().getClazz().getClassId();
                List<Enrollment> enrollments = enrollmentRepository.findByClazzClassId(classId);
                for (Enrollment enrollment : enrollments) {
                    if (enrollment.getStudent() != null) {
                        uniqueStudentIds.add(enrollment.getStudent().getUserId());
                    }
                }
                log.debug("Class ID {} has {} enrollments", classId, enrollments.size());
            }
        }
        
        int totalStudents = uniqueStudentIds.size();
        log.info("Total unique students (from enrollments): {}", totalStudents);
        
        // 4. Calculate class summaries and overall performance
        List<ClassSummaryDTO> classSummaries = new ArrayList<>();
        double totalAvgScore = 0.0;
        int totalPassCount = 0;
        int totalExcellentCount = 0;
        int totalGradedStudents = 0;
        
        // Group ClassSubjects by Class to avoid duplicates
        Map<Long, List<ClassSubject>> classSubjectsByClass = classSubjects.stream()
                .filter(cs -> cs.getTerm() != null && cs.getTerm().getClazz() != null)
                .collect(Collectors.groupingBy(cs -> cs.getTerm().getClazz().getClassId()));
        
        log.info("Found {} unique classes", classSubjectsByClass.size());
        
        for (Map.Entry<Long, List<ClassSubject>> entry : classSubjectsByClass.entrySet()) {
            Long classId = entry.getKey();
            List<ClassSubject> classSubjectsForClass = entry.getValue();
            
            // Get the first ClassSubject to access Class info
            ClassSubject firstCs = classSubjectsForClass.get(0);
            Clazz clazz = firstCs.getTerm().getClazz();
            
            log.info("Processing class: {} (ID: {})", clazz.getName(), clazz.getClassId());
            
            // Get enrolled students count for this class
            List<Enrollment> classEnrollments = enrollmentRepository.findByClazzClassId(classId);
            int enrolledStudentCount = classEnrollments.size();
            log.info("Class {} has {} enrolled students", clazz.getName(), enrolledStudentCount);
            
            // Collect all grades from all ClassSubjects for this class
            List<SubjectGrade> allGradesForClass = new ArrayList<>();
            for (ClassSubject cs : classSubjectsForClass) {
                List<SubjectGrade> grades = subjectGradeRepository.findByClassSubject_ClassSubjectId(cs.getClassSubjectId());
                allGradesForClass.addAll(grades);
                log.debug("ClassSubject ID {}: found {} grades", cs.getClassSubjectId(), grades.size());
            }
            
            log.info("Class {} total grades: {}", clazz.getName(), allGradesForClass.size());
            
            // Calculate class metrics (even if no grades, still create class entry)
            double classAvgScore = 0.0;
            int passCount = 0;
            int excellentCount = 0;
            int validGradeCount = 0;
            
            // Only calculate if there are grades
            if (!allGradesForClass.isEmpty()) {
                for (SubjectGrade grade : allGradesForClass) {
                    Float avgScore = grade.getActualAverage();
                    if (avgScore != null && avgScore > 0) {
                        classAvgScore += avgScore;
                        validGradeCount++;
                        
                        if (avgScore >= 5.0) {
                            passCount++;
                        }
                        if (avgScore >= 9.0) {
                            excellentCount++;
                        }
                    }
                }
                
                if (validGradeCount > 0) {
                    classAvgScore /= validGradeCount;
                    
                    // Add to overall totals
                    totalAvgScore += classAvgScore * validGradeCount;
                    totalPassCount += passCount;
                    totalExcellentCount += excellentCount;
                    totalGradedStudents += validGradeCount;
                }
            }
            
            // Create class summary REGARDLESS of whether grades exist
            // Use enrolled student count, not graded student count
            ClassSummaryDTO classSummary = ClassSummaryDTO.builder()
                    .classId(clazz.getClassId())
                    .className(clazz.getName())
                    .gradeLevel(clazz.getGradeLevel() != null ? clazz.getGradeLevel().toString() : "N/A")
                    .studentCount(enrolledStudentCount)  // Total enrolled students, not just graded
                    .avgScore(validGradeCount > 0 ? Math.round(classAvgScore * 100.0) / 100.0 : 0.0)
                    .passRate(validGradeCount > 0 ? Math.round((double) passCount / validGradeCount * 10000.0) / 100.0 : 0.0)
                    .excellentRate(validGradeCount > 0 ? Math.round((double) excellentCount / validGradeCount * 10000.0) / 100.0 : 0.0)
                    .trend("STABLE")
                    .build();
            
            classSummaries.add(classSummary);
            
            log.info("Added class {}: enrolledStudents={}, gradedStudents={}, avgScore={}, passRate={}, excellentRate={}", 
                    clazz.getName(), 
                    enrolledStudentCount,
                    validGradeCount,
                    classSummary.getAvgScore(), 
                    classSummary.getPassRate(), 
                    classSummary.getExcellentRate());
        }
        
        // 5. Calculate overall metrics
        double overallAvgScore = totalGradedStudents > 0 ? 
                Math.round((totalAvgScore / totalGradedStudents) * 100.0) / 100.0 : 0.0;
        double overallPassRate = totalGradedStudents > 0 ? 
                Math.round(((double) totalPassCount / totalGradedStudents * 100.0) * 100.0) / 100.0 : 0.0;
        double overallExcellentRate = totalGradedStudents > 0 ? 
                Math.round(((double) totalExcellentCount / totalGradedStudents * 100.0) * 100.0) / 100.0 : 0.0;
        
        log.info("Overall metrics - avgScore: {}, passRate: {}%, excellentRate: {}%", 
                overallAvgScore, overallPassRate, overallExcellentRate);
        
        // 6. Compare to school-wide averages (placeholder - implement later)
        double schoolAvgScore = overallAvgScore; // TODO: Calculate real school average
        double schoolPassRate = overallPassRate;
        double schoolExcellentRate = overallExcellentRate;
        
        TeacherClassOverviewDTO.TeacherInfo teacherInfo = TeacherClassOverviewDTO.TeacherInfo.builder()
                .teacherId(teacher.getUserId())
                .teacherName(teacher.getFullName())
                .subject(mainSubject)
                .totalStudents(totalStudents)
                .build();
        
        OverallPerformanceDTO overallPerformance = OverallPerformanceDTO.builder()
                .avgScore(overallAvgScore)
                .passRate(overallPassRate)
                .excellentRate(overallExcellentRate)
                .comparisonToSchool(OverallPerformanceDTO.ComparisonDTO.builder()
                        .avgScoreDiff(Math.round((overallAvgScore - schoolAvgScore) * 100.0) / 100.0)
                        .passRateDiff(Math.round((overallPassRate - schoolPassRate) * 100.0) / 100.0)
                        .excellentRateDiff(Math.round((overallExcellentRate - schoolExcellentRate) * 100.0) / 100.0)
                        .build())
                .build();
        
        return TeacherClassOverviewDTO.builder()
                .teacherInfo(teacherInfo)
                .overallPerformance(overallPerformance)
                .classes(classSummaries)
                .build();
    }
    
    /**
     * Get detailed analytics for a specific class
     * @param classId ID of the class
     * @param teacherId ID of the teacher (for authorization)
     * @return Detailed class performance including student list
     */
    public ClassSummaryDTO getClassDetailedAnalytics(Long classId, Long teacherId) {
        log.info("Fetching detailed analytics for class ID: {} by teacher ID: {}", classId, teacherId);
        
        // TODO: Implement:
        // 1. Verify teacher teaches this class
        // 2. Get all students in the class
        // 3. Calculate avg score, pass rate, excellent rate
        // 4. Determine performance trend (compare recent vs older assignments)
        
        return ClassSummaryDTO.builder()
                .classId(classId)
                .className("N/A")
                .gradeLevel("N/A")
                .studentCount(0)
                .avgScore(0.0)
                .passRate(0.0)
                .excellentRate(0.0)
                .trend("STABLE")
                .build();
    }
    
    /**
     * Get individual student performance in teacher's subject
     * @param studentId ID of the student
     * @param teacherId ID of the teacher
     * @return Student performance across topics with weak/strong areas
     */
    public StudentPerformanceDTO getStudentPerformance(Long studentId, Long teacherId) {
        log.info("Fetching student ID: {} performance for teacher ID: {}", studentId, teacherId);
        
        // TODO: Implement:
        // 1. Verify student is in one of teacher's classes
        // 2. Get all assignment submissions by student in teacher's subject
        // 3. Calculate topic-wise scores
        // 4. Identify weak topics (score < 5.0)
        // 5. Identify strong topics (score >= 9.0)
        // 6. Determine progress trend
        
        return StudentPerformanceDTO.builder()
                .studentId(studentId)
                .studentName("N/A")
                .studentCode("N/A")
                .overallScore(0.0)
                .topicScores(java.util.Collections.emptyList())
                .weakTopics(java.util.Collections.emptyList())
                .strongTopics(java.util.Collections.emptyList())
                .progressTrend("STABLE")
                .build();
    }
}
