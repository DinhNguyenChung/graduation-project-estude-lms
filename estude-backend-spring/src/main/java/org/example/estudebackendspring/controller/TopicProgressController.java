package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.SubmissionWithTopicsDTO;
import org.example.estudebackendspring.dto.TopicDTO;
import org.example.estudebackendspring.dto.TopicProgressDTO;
import org.example.estudebackendspring.dto.TopicProgressSummaryDTO;
import org.example.estudebackendspring.service.TopicProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topic-progress")
public class TopicProgressController {
    
    private final TopicProgressService topicProgressService;
    
    public TopicProgressController(TopicProgressService topicProgressService) {
        this.topicProgressService = topicProgressService;
    }
    
    /**
     * GET /api/topic-progress/student/{studentId}/subject/{subjectId}
     * Lấy tổng quan tiến độ học tập theo subject
     */
    @GetMapping("/student/{studentId}/subject/{subjectId}")
    public ResponseEntity<TopicProgressSummaryDTO> getProgressSummary(
            @PathVariable Long studentId,
            @PathVariable Long subjectId) {
        return ResponseEntity.ok(
            topicProgressService.getProgressSummary(studentId, subjectId));
    }
    
    /**
     * GET /api/topic-progress/student/{studentId}/topic/{topicId}
     * Lấy lịch sử của 1 topic
     */
    @GetMapping("/student/{studentId}/topic/{topicId}")
    public ResponseEntity<List<TopicProgressDTO>> getTopicHistory(
            @PathVariable Long studentId,
            @PathVariable Long topicId) {
        return ResponseEntity.ok(
            topicProgressService.getTopicHistory(studentId, topicId));
    }
    
    /**
     * GET /api/topic-progress/submission/{submissionId}
     * Lấy kết quả theo topics của 1 submission
     */
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<SubmissionWithTopicsDTO> getSubmissionTopicBreakdown(
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(
            topicProgressService.getSubmissionTopicBreakdown(submissionId));
    }
    
    /**
     * GET /api/topic-progress/student/{studentId}/weak-topics
     * Lấy các topics yếu cần cải thiện
     * Query param: subjectId, threshold (default: 0.6)
     */
    @GetMapping("/student/{studentId}/weak-topics")
    public ResponseEntity<List<TopicDTO>> getWeakTopics(
            @PathVariable Long studentId,
            @RequestParam Long subjectId,
            @RequestParam(defaultValue = "0.6") Float threshold) {
        return ResponseEntity.ok(
            topicProgressService.getWeakTopics(studentId, subjectId, threshold));
    }
}
