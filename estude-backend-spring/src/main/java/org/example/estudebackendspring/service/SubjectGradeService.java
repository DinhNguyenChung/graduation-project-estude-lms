package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.SubjectGrade;
import org.example.estudebackendspring.repository.SubjectGradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectGradeService {
    private final SubjectGradeRepository subjectGradeRepository;

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
}