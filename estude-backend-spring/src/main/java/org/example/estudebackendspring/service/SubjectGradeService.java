package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.SubjectGradeDTO;
import org.example.estudebackendspring.dto.SubjectGradeRequest;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.SubjectGrade;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.repository.SubjectGradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectGradeService {
    private final SubjectGradeRepository subjectGradeRepository;
    private final StudentRepository studentRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private static final double REGULAR_WEIGHT = 0.10;
    private static final double MIDTERM_WEIGHT = 0.20;
    private static final double FINAL_WEIGHT = 0.30;
    public List<SubjectGrade> getByClassSubject(Long classSubjectId) {
        return subjectGradeRepository.findByClassSubjectIdWithStudent(classSubjectId);
    }

    @Transactional
    public SubjectGrade updateSubjectGrade(Long subjectGradeId,
                                           List<Float> regularScores,
                                           Float midtermScore,
                                           Float finalScore,
                                           String comment) {
        SubjectGrade subjectGrade = subjectGradeRepository.findById(subjectGradeId)
                .orElseThrow(() -> new RuntimeException("SubjectGrade not found"));

        subjectGrade.setRegularScores(regularScores);
        subjectGrade.setMidtermScore(midtermScore);
        subjectGrade.setFinalScore(finalScore);
        subjectGrade.setComment(comment);

        // Tính average
        float sum = 0;
        if (regularScores != null && !regularScores.isEmpty()) {
            for (Float s : regularScores) sum += s;
            subjectGrade.setActualAverage((sum / regularScores.size() + midtermScore + finalScore) / 3);
        }

        return subjectGradeRepository.save(subjectGrade);
    }
    @Transactional
    public SubjectGradeDTO upsertSubjectGrade(SubjectGradeRequest req) {
        // Validate student and classSubject exist
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.getStudentId()));

        ClassSubject cs = classSubjectRepository.findById(req.getClassSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found: " + req.getClassSubjectId()));

        // Find existing grade (student + classSubject)
        Optional<SubjectGrade> existingOpt = subjectGradeRepository
                .findByStudent_UserIdAndClassSubject_ClassSubjectId(req.getStudentId(), req.getClassSubjectId());

        SubjectGrade grade = existingOpt.orElseGet(SubjectGrade::new);

        // Set relations if created
        grade.setStudent(student);
        grade.setClassSubject(cs);

        // Update fields
        if (req.getRegularScores() != null) {
            grade.setRegularScores(new ArrayList<>(req.getRegularScores()));
        }
        grade.setMidtermScore(req.getMidtermScore());
        grade.setFinalScore(req.getFinalScore());
        grade.setComment(req.getComment());

        // Compute averages
        double regularAvg = computeRegularAverage(grade.getRegularScores()); // returns NaN if no regulars
        Float mid = grade.getMidtermScore();
        Float fin = grade.getFinalScore();

        // NEW RULE:
        // - If final (fin) is NOT provided -> actualAverage stays null (do NOT compute).
        // - If final IS provided -> treat missing mid or regular as 0 and compute using formula:
        //   actual = (1*regularAvg + 2*mid + 3*final) / 6
        Float actualAverage = null;
        if (fin != null) {
            double regularForCalc = Double.isNaN(regularAvg) ? 0.0 : regularAvg;
            double midForCalc = (mid == null) ? 0.0 : mid.doubleValue();
            double finalForCalc = fin.doubleValue();

            double raw = (1.0 * regularForCalc + 2.0 * midForCalc + 3.0 * finalForCalc) / 6.0;

            // Round to 2 decimal places
            BigDecimal bd = BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP);
            actualAverage = bd.floatValue();
        } else {
            actualAverage = null;
        }

        // DON'T set predicted fields here (AI handled elsewhere).
        grade.setActualAverage(actualAverage);
        // keep predicted fields unchanged

        SubjectGrade saved = subjectGradeRepository.save(grade);

        return toDto(saved);
    }



    public SubjectGradeDTO getSubjectGrade(Long gradeId) {
        SubjectGrade g = subjectGradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("SubjectGrade not found: " + gradeId));
        return toDto(g);
    }

    public Optional<SubjectGradeDTO> findByStudentAndClassSubject(Long studentId, Long classSubjectId) {
        return subjectGradeRepository
                .findByStudent_UserIdAndClassSubject_ClassSubjectId(studentId, classSubjectId)
                .map(this::toDto);
    }

    // Utils
    private double computeRegularAverage(List<Float> scores) {
        if (scores == null || scores.isEmpty()) return Double.NaN;
        double sum = 0.0;
        int count = 0;
        for (Float f : scores) {
            if (f != null) { sum += f; count++; }
        }
        return count == 0 ? Double.NaN : sum / count;
    }

    private SubjectGradeDTO toDto(SubjectGrade g) {
        SubjectGradeDTO dto = new SubjectGradeDTO();
        dto.setSubjectGradeId(g.getSubjectGradeId());
        dto.setStudentId(g.getStudent() != null ? g.getStudent().getUserId() : null);
        dto.setClassSubjectId(g.getClassSubject() != null ? g.getClassSubject().getClassSubjectId() : null);
        dto.setRegularScores(g.getRegularScores());
        dto.setMidtermScore(g.getMidtermScore());
        dto.setFinalScore(g.getFinalScore());
        dto.setActualAverage(g.getActualAverage());
//        dto.setPredictedMidTerm(g.getPredictedMidTerm());
//        dto.setPredictedFinal(g.getPredictedFinal());
//        dto.setPredictedAverage(g.getPredictedAverage());
//        dto.setComment(g.getComment());
        return dto;
    }
}