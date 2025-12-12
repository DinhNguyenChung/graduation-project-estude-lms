package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.StudentGradeResponse;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.SubjectGrade;
import org.example.estudebackendspring.repository.SubjectGradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherGradeService {

    private final SubjectGradeRepository subjectGradeRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<StudentGradeResponse> getGradesByClassSubject(Long classSubjectId) {
        List<SubjectGrade> grades = subjectGradeRepository.findByClassSubject_ClassSubjectId(classSubjectId);

        return grades.stream().map(g -> {
            Student s = g.getStudent();
            return new StudentGradeResponse(
                    s.getUserId(),
                    s.getStudentCode(),
                    s.getFullName(),
                    g.getRegularScores(),
                    g.getMidtermScore(),
                    g.getFinalScore(),
                    g.getActualAverage(),
                    g.getPredictedMidTerm(),
                    g.getPredictedFinal(),
                    g.getPredictedAverage(),
                    g.getComment()
            );
        }).collect(Collectors.toList());
    }
}
