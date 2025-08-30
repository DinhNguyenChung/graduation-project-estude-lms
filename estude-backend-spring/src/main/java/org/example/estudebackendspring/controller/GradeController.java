package org.example.estudebackendspring.controller;


import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.dto.GradeUpdateRequest;
import org.example.estudebackendspring.entity.Grade;
import org.example.estudebackendspring.entity.SubjectGrade;
import org.example.estudebackendspring.service.GradeService;
import org.example.estudebackendspring.service.SubjectGradeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GradeController {
    private final GradeService gradeService;
    private final SubjectGradeService subjectGradeService;

    // POST /api/grades -> assign grade
    @PostMapping("/grades")
    public ResponseEntity<?> assignGrade(@RequestParam Long submissionId,
                                         @RequestParam Long teacherId,
                                         @RequestParam Float score,
                                         @RequestParam(required = false) String feedback) {
        try {
            Grade grade = gradeService.assignGrade(submissionId, score, feedback, teacherId);
            return ResponseEntity.ok(new AuthResponse(true, "Grade assigned", grade));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }

    // PUT /api/grades/{gradeId} -> update grade
    @PutMapping(value = "/grades/{gradeId}" ,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateGrade(@PathVariable Long gradeId,
                                         @RequestParam Float score,
                                         @RequestParam(required = false) String feedback) {
        try {
            Grade grade = gradeService.updateGrade(gradeId, score, feedback);
            return ResponseEntity.ok(new AuthResponse(true, "Grade updated", grade));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
//    @PutMapping(value = "/grades/{gradeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> updateGrade(@PathVariable Long gradeId,
//                                         @RequestBody GradeUpdateRequest req) {
//        try {
//            Grade grade = gradeService.updateGrade(gradeId, req.getScore(), req.getFeedback());
//            return ResponseEntity.ok(new AuthResponse(true, "Grade updated", grade));
//        } catch (RuntimeException e) {
//            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
//        }
//    }


    // GET /api/class-subjects/{classSubjectId}/subject-grades
    @GetMapping("/class-subjects/{classSubjectId}/subject-grades")
    public ResponseEntity<?> getSubjectGrades(@PathVariable Long classSubjectId) {
        List<SubjectGrade> list = subjectGradeService.getByClassSubject(classSubjectId);
        return ResponseEntity.ok(new AuthResponse(true, "Subject grades retrieved", list));
    }

    // PUT /api/subject-grades/{subjectGradeId}
    @PutMapping("/subject-grades/{subjectGradeId}")
    public ResponseEntity<?> updateSubjectGrade(@PathVariable Long subjectGradeId,
                                                @RequestBody SubjectGrade updated) {
        try {
            SubjectGrade sg = subjectGradeService.updateSubjectGrade(
                    subjectGradeId,
                    updated.getRegularScores(),
                    updated.getMidtermScore(),
                    updated.getFinalScore(),
                    updated.getComment()
            );
            return ResponseEntity.ok(new AuthResponse(true, "Subject grade updated", sg));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new AuthResponse(false, e.getMessage(), null));
        }
    }
}
