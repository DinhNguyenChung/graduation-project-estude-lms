package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.SubjectGradeDTO;
import org.example.estudebackendspring.dto.SubjectGradeRequest;
import org.example.estudebackendspring.dto.TermGradesDTO;
import org.example.estudebackendspring.service.SubjectGradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/subject-grades")
@Validated
public class SubjectGradeController {

    private final SubjectGradeService subjectGradeService;

    public SubjectGradeController(SubjectGradeService subjectGradeService) {
        this.subjectGradeService = subjectGradeService;
    }

    /**
     * Create or update subject grade for a student in a classSubject
     */
    @PostMapping
    public ResponseEntity<SubjectGradeDTO> upsertGrade(@RequestBody @Validated SubjectGradeRequest req) {
        SubjectGradeDTO dto = subjectGradeService.upsertSubjectGrade(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Get grade by id
     */
    @GetMapping("/{gradeId}")
    public ResponseEntity<SubjectGradeDTO> getGrade(@PathVariable Long gradeId) {
        SubjectGradeDTO dto = subjectGradeService.getSubjectGrade(gradeId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Get grade by studentId + classSubjectId (optional)
     */
    @GetMapping("/student/{studentId}/class-subject/{classSubjectId}")
    public ResponseEntity<?> getByStudentAndClassSubject(@PathVariable Long studentId,
                                                         @PathVariable Long classSubjectId) {
        Optional<SubjectGradeDTO> opt = subjectGradeService.findByStudentAndClassSubject(studentId, classSubjectId);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            // Trả về mảng rỗng thay vì message + status
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    /**
     * GET /api/grades/student/{studentId}/by-terms
     * Trả về tất cả bảng điểm của học sinh, nhóm theo kỳ.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TermGradesDTO>> getAllGradesByStudentGroupedByTerm(@PathVariable Long studentId) {
        List<TermGradesDTO> data = subjectGradeService.getAllGradesGroupedByTerm(studentId);
        return ResponseEntity.ok(data);
    }

}