package org.example.estudebackendspring.service;


import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;

    public QuestionService(QuestionRepository questionRepository, AssignmentRepository assignmentRepository) {
        this.questionRepository = questionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public Question addQuestion(Long assignmentId, Question question) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        question.setAssignment(assignment);

        // gán quan hệ 2 chiều cho options
        if (question.getOptions() != null) {
            for (QuestionOption option : question.getOptions()) {
                option.setQuestion(question);
            }
        }

        return questionRepository.save(question);
    }


    @Transactional
    public Question updateQuestion(Long questionId, Question updated) {
        Question existing = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        existing.setQuestionText(updated.getQuestionText());
        existing.setPoints(updated.getPoints());
        existing.setQuestionType(updated.getQuestionType());
        existing.setQuestionOrder(updated.getQuestionOrder());
        existing.setAttachmentUrl(updated.getAttachmentUrl());

        // Xóa options cũ
        existing.getOptions().clear();

        // Thêm options mới
        if (updated.getOptions() != null) {
            for (QuestionOption opt : updated.getOptions()) {
                opt.setQuestion(existing);
                existing.getOptions().add(opt);
            }
        }

        return questionRepository.save(existing);
    }



    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(questionId);
    }
}