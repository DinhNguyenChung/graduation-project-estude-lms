package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.CreatePracticeTestRequest;
import org.example.estudebackendspring.dto.PracticeTestDTO;
import org.example.estudebackendspring.dto.SubmissionRequest;
import org.example.estudebackendspring.dto.SubmissionWithTopicsDTO;
import org.example.estudebackendspring.service.PracticeTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice-tests")
public class PracticeTestController {
    
    private final PracticeTestService practiceTestService;
    
    public PracticeTestController(PracticeTestService practiceTestService) {
        this.practiceTestService = practiceTestService;
    }
    
    /**
     * POST /api/practice-tests/create
     * Tạo bài test luyện tập
     */
    @PostMapping("/create")
    public ResponseEntity<PracticeTestDTO> createPracticeTest(
            @RequestBody CreatePracticeTestRequest request) {
        return ResponseEntity.ok(practiceTestService.createPracticeTest(request));
    }
    
    /**
     * GET /api/practice-tests/{testId}
     * Lấy chi tiết bài test
     */
    @GetMapping("/{testId}")
    public ResponseEntity<PracticeTestDTO> getPracticeTest(@PathVariable Long testId) {
        return ResponseEntity.ok(practiceTestService.getPracticeTest(testId));
    }
    
    /**
     * GET /api/practice-tests/student/{studentId}
     * Lấy danh sách bài test của học sinh
     * Query param: completed (true/false/null)
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PracticeTestDTO>> getStudentPracticeTests(
            @PathVariable Long studentId,
            @RequestParam(required = false) Boolean completed) {
        return ResponseEntity.ok(
            practiceTestService.getStudentPracticeTests(studentId, completed));
    }
    
    /**
     * POST /api/practice-tests/{testId}/submit
     * Nộp bài test
     */
    @PostMapping("/{testId}/submit")
    public ResponseEntity<SubmissionWithTopicsDTO> submitPracticeTest(
            @PathVariable Long testId,
            @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(practiceTestService.submitPracticeTest(testId, request));
    }
    
    /**
     * DELETE /api/practice-tests/{testId}
     * Xóa bài test (chỉ khi chưa làm)
     */
    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> deletePracticeTest(@PathVariable Long testId) {
        practiceTestService.deletePracticeTest(testId);
        return ResponseEntity.noContent().build();
    }
}
