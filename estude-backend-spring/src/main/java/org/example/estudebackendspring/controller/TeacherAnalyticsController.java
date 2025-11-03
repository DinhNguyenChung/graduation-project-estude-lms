package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.analytics.*;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.TeacherAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Subject Teacher Analytics Dashboard
 * Teachers can view their classes, students, and performance statistics
 */
@RestController
@RequestMapping("/api/teacher/analytics")
@RequiredArgsConstructor
@Slf4j
//@PreAuthorize("hasRole('TEACHER')")
public class TeacherAnalyticsController {
    
    private final TeacherAnalyticsService teacherAnalyticsService;
    private final LogEntryService logEntryService;
    
    /**
     * GET /api/teacher/analytics/overview
     * Get overview of all classes taught by the logged-in teacher
     * 
     * Response:
     * {
     *   "teacher_info": {
     *     "teacher_id": 5,
     *     "teacher_name": "Nguyễn Văn A",
     *     "subject": "Toán",
     *     "total_students": 150
     *   },
     *   "overall_performance": {
     *     "avg_score": 7.2,
     *     "pass_rate": 85.3,
     *     "excellent_rate": 23.5,
     *     "comparison_to_school": {
     *       "avg_score_diff": +0.5,
     *       "pass_rate_diff": +3.2,
     *       "excellent_rate_diff": -1.8
     *     }
     *   },
     *   "classes": [
     *     {
     *       "class_id": 12,
     *       "class_name": "10A1",
     *       "grade_level": "10",
     *       "student_count": 35,
     *       "avg_score": 7.5,
     *       "pass_rate": 88.6,
     *       "excellent_rate": 25.7,
     *       "trend": "IMPROVING"
     *     }
     *   ]
     * }
     */
    @GetMapping("/overview")
    public ResponseEntity<TeacherClassOverviewDTO> getTeacherOverview(
            @RequestParam Long teacherId) {
        try {
            log.info("Teacher ID: {} requesting overview", teacherId);
            
            TeacherClassOverviewDTO overview = teacherAnalyticsService.getTeacherOverview(teacherId);
            

            
            return ResponseEntity.ok(overview);
            
        } catch (RuntimeException e) {
            log.error("Error fetching teacher overview: {}", e.getMessage());
            

            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Internal error fetching teacher overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/teacher/analytics/classes/{classId}
     * Get detailed analytics for a specific class
     * 
     * Response:
     * {
     *   "class_id": 12,
     *   "class_name": "10A1",
     *   "grade_level": "10",
     *   "student_count": 35,
     *   "avg_score": 7.5,
     *   "pass_rate": 88.6,
     *   "excellent_rate": 25.7,
     *   "trend": "IMPROVING"
     * }
     */
    @GetMapping("/classes/{classId}")
    public ResponseEntity<ClassSummaryDTO> getClassAnalytics(
            @PathVariable Long classId,
            @RequestParam Long teacherId) {
        try {
            log.info("Teacher ID: {} requesting analytics for class ID: {}", teacherId, classId);
            
            ClassSummaryDTO classAnalytics = teacherAnalyticsService.getClassDetailedAnalytics(classId, teacherId);
            

            
            return ResponseEntity.ok(classAnalytics);
            
        } catch (RuntimeException e) {
            log.error("Error fetching class analytics: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Internal error fetching class analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/teacher/analytics/students/{studentId}
     * Get individual student performance in teacher's subject
     * 
     * Response:
     * {
     *   "student_id": 123,
     *   "student_name": "Trần Thị B",
     *   "student_code": "HS001",
     *   "overall_score": 6.5,
     *   "topic_scores": [
     *     {
     *       "topic_name": "Hàm Số",
     *       "score": 8.0,
     *       "completed_assignments": 5,
     *       "total_assignments": 5
     *     },
     *     {
     *       "topic_name": "Phương Trình",
     *       "score": 4.5,
     *       "completed_assignments": 3,
     *       "total_assignments": 5
     *     }
     *   ],
     *   "weak_topics": [
     *     {
     *       "topic_name": "Phương Trình",
     *       "score": 4.5,
     *       "recommended_resources": ["Bài tập bổ trợ", "Video hướng dẫn"]
     *     }
     *   ],
     *   "strong_topics": ["Hàm Số", "Đạo Hàm"],
     *   "progress_trend": "DECLINING"
     * }
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(
            @PathVariable Long studentId,
            @RequestParam Long teacherId) {
        try {
            log.info("Teacher ID: {} requesting performance for student ID: {}", teacherId, studentId);
            
            StudentPerformanceDTO performance = teacherAnalyticsService.getStudentPerformance(studentId, teacherId);
            

            
            return ResponseEntity.ok(performance);
            
        } catch (RuntimeException e) {
            log.error("Error fetching student performance: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Internal error fetching student performance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
