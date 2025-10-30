package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.TopicDTO;
import org.example.estudebackendspring.enums.GradeLevel;
import org.example.estudebackendspring.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    
    private final TopicService topicService;
    
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }
    
    /**
     * GET /api/topics?subjectId=1&gradeLevel=GRADE_10&volume=1
     * Lấy danh sách topics theo môn học, khối và tập
     * Parameters:
     * - subjectId: required
     * - gradeLevel: optional (GRADE_10, GRADE_11, GRADE_12)
     * - volume: optional (1, 2, 3...)
     */
    @GetMapping
    public ResponseEntity<List<TopicDTO>> getTopics(
            @RequestParam Long subjectId,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) Integer volume) {
        
        // Case 1: Filter by subject, grade AND volume
        if (gradeLevel != null && volume != null) {
            GradeLevel grade = GradeLevel.valueOf(gradeLevel);
            return ResponseEntity.ok(
                topicService.getTopicsBySubjectGradeAndVolume(subjectId, grade, volume));
        }
        
        // Case 2: Filter by subject AND grade only
        if (gradeLevel != null) {
            GradeLevel grade = GradeLevel.valueOf(gradeLevel);
            return ResponseEntity.ok(
                topicService.getTopicsBySubjectAndGrade(subjectId, grade));
        }
        
        // Case 3: Filter by subject AND volume only
        if (volume != null) {
            return ResponseEntity.ok(
                topicService.getTopicsBySubjectAndVolume(subjectId, volume));
        }
        
        // Case 4: Get all topics for subject
        return ResponseEntity.ok(topicService.getTopicsBySubject(subjectId));
    }
    
    /**
     * GET /api/topics/grades?subjectId=1
     * Lấy danh sách các khối có sẵn cho môn học
     */
    @GetMapping("/grades")
    public ResponseEntity<List<String>> getAvailableGrades(@RequestParam Long subjectId) {
        return ResponseEntity.ok(topicService.getAvailableGradeLevels(subjectId));
    }
    
    /**
     * GET /api/topics/volumes?subjectId=1&gradeLevel=GRADE_10
     * Lấy danh sách tập có sẵn cho môn học và khối
     */
    @GetMapping("/volumes")
    public ResponseEntity<List<Integer>> getAvailableVolumes(
            @RequestParam Long subjectId,
            @RequestParam String gradeLevel) {
        GradeLevel grade = GradeLevel.valueOf(gradeLevel);
        return ResponseEntity.ok(topicService.getAvailableVolumes(subjectId, grade));
    }
    
    /**
     * GET /api/topics/{topicId}
     * Lấy chi tiết topic
     */
    @GetMapping("/{topicId}")
    public ResponseEntity<TopicDTO> getTopicDetail(@PathVariable Long topicId) {
        return ResponseEntity.ok(topicService.getTopicById(topicId));
    }
    
    /**
     * POST /api/topics
     * Tạo topic mới (Admin/Teacher only)
     */
    @PostMapping
    public ResponseEntity<TopicDTO> createTopic(@RequestBody TopicDTO topicDTO) {
        return ResponseEntity.ok(topicService.createTopic(topicDTO));
    }
    
    /**
     * PUT /api/topics/{topicId}
     * Cập nhật topic
     */
    @PutMapping("/{topicId}")
    public ResponseEntity<TopicDTO> updateTopic(
            @PathVariable Long topicId,
            @RequestBody TopicDTO topicDTO) {
        return ResponseEntity.ok(topicService.updateTopic(topicId, topicDTO));
    }
    
    /**
     * DELETE /api/topics/{topicId}
     * Xóa topic
     */
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
        topicService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }
}
