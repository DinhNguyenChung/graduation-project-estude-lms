package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.analytics.HomeroomClassDTO;
import org.example.estudebackendspring.dto.analytics.StudentPerformanceDTO;
import org.example.estudebackendspring.service.HomeroomTeacherAnalyticsService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Homeroom Teacher Analytics Dashboard
 * Homeroom teachers can view comprehensive class overview across all subjects
 */
@RestController
@RequestMapping("/api/homeroom/analytics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TEACHER')")
public class HomeroomTeacherAnalyticsController {
    
    private final HomeroomTeacherAnalyticsService homeroomAnalyticsService;
    private final LogEntryService logEntryService;
    
    /**
     * GET /api/homeroom/analytics/my-class
     * Get comprehensive overview of homeroom class
     * 
     * Response:
     * {
     *   "class_id": 12,
     *   "class_name": "10A1",
     *   "grade_level": "10",
     *   "homeroom_teacher": "Nguyễn Văn A",
     *   "student_count": 35,
     *   "overall_performance": {
     *     "avg_score": 7.2,
     *     "pass_rate": 85.3,
     *     "excellent_rate": 23.5,
     *     "comparison_to_school": { ... }
     *   },
     *   "subject_performance": [
     *     {
     *       "subject_name": "Toán",
     *       "teacher_name": "Trần Thị B",
     *       "avg_score": 7.5,
     *       "pass_rate": 88.6,
     *       "excellent_rate": 25.7
     *     },
     *     {
     *       "subject_name": "Vật Lý",
     *       "teacher_name": "Lê Văn C",
     *       "avg_score": 6.8,
     *       "pass_rate": 80.0,
     *       "excellent_rate": 20.0
     *     }
     *   ],
     *   "top_performers": [
     *     {
     *       "student_id": 123,
     *       "student_name": "Nguyễn Văn D",
     *       "student_code": "HS001",
     *       "overall_score": 9.2,
     *       "rank": 1
     *     }
     *   ],
     *   "at_risk_students": [
     *     {
     *       "student_id": 456,
     *       "student_name": "Trần Thị E",
     *       "student_code": "HS045",
     *       "overall_score": 4.3,
     *       "rank": 35
     *     }
     *   ]
     * }
     */
    @GetMapping("/my-class")
    public ResponseEntity<HomeroomClassDTO> getHomeroomClassOverview(
            @RequestParam Long classId,
            @RequestParam Long teacherId) {
        try {
            log.info("Homeroom teacher ID: {} requesting overview for class ID: {}", teacherId, classId);
            
            HomeroomClassDTO overview = homeroomAnalyticsService.getHomeroomClassOverview(classId, teacherId);
            

            
            return ResponseEntity.ok(overview);
            
        } catch (RuntimeException e) {
            log.error("Error fetching homeroom class overview: {}", e.getMessage());
            

            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Internal error fetching homeroom class overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/homeroom/analytics/students/{studentId}
     * Get complete performance of a student across all subjects
     * 
     * Response:
     * {
     *   "student_id": 123,
     *   "student_name": "Nguyễn Văn D",
     *   "student_code": "HS001",
     *   "overall_score": 7.5,
     *   "topic_scores": [
     *     {
     *       "topic_name": "Toán - Hàm Số",
     *       "score": 8.0,
     *       "completed_assignments": 5,
     *       "total_assignments": 5
     *     },
     *     {
     *       "topic_name": "Vật Lý - Động Lực Học",
     *       "score": 6.5,
     *       "completed_assignments": 4,
     *       "total_assignments": 5
     *     }
     *   ],
     *   "weak_topics": [
     *     {
     *       "topic_name": "Hóa Học - Hóa Hữu Cơ",
     *       "score": 4.5,
     *       "recommended_resources": [...]
     *     }
     *   ],
     *   "strong_topics": ["Toán - Hàm Số", "Văn - Văn Học"],
     *   "progress_trend": "IMPROVING"
     * }
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentPerformanceDTO> getStudentCompletePerformance(
            @PathVariable Long studentId,
            @RequestParam Long teacherId) {
        try {
            log.info("Homeroom teacher ID: {} requesting complete performance for student ID: {}", 
                    teacherId, studentId);
            
            StudentPerformanceDTO performance = homeroomAnalyticsService.getStudentCompletePerformance(
                    studentId, teacherId);
            
            
            return ResponseEntity.ok(performance);
            
        } catch (RuntimeException e) {
            log.error("Error fetching student complete performance: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Internal error fetching student complete performance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
