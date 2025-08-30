package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.Grade;
import org.example.estudebackendspring.entity.Submission;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {
    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public Grade assignGrade(Long submissionId, Float score, String feedback, Long teacherId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setTeacher(teacher);
        grade.setScore(score);
        grade.setAutoGradedFeedback(feedback);
        grade.setGradedAt(LocalDateTime.now());
        grade.setIsAutoGraded(false);

        return gradeRepository.save(grade);
    }

    @Transactional
    public Grade updateGrade(Long gradeId, Float score, String feedback) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));

        grade.setScore(score);
        grade.setAutoGradedFeedback(feedback);
        grade.setGradedAt(LocalDateTime.now());

        return gradeRepository.save(grade);
    }
}