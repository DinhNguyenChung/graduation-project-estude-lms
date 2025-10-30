package org.example.estudebackendspring.service;


import org.example.estudebackendspring.dto.QuestionBankRequest;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.QuestionType;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;
    private final TopicRepository topicRepository;

    public QuestionService(QuestionRepository questionRepository, 
                          AssignmentRepository assignmentRepository,
                          TopicRepository topicRepository) {
        this.questionRepository = questionRepository;
        this.assignmentRepository = assignmentRepository;
        this.topicRepository = topicRepository;
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

    /**
     * Tạo câu hỏi cho Question Bank (gắn với Topic)
     */
    @Transactional
    public Question createQuestionBank(QuestionBankRequest request) {
        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
            .orElseThrow(() -> new RuntimeException("Topic not found with id: " + request.getTopicId()));
        
        // Create question
        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setPoints(request.getPoints());
        question.setQuestionType(QuestionType.valueOf(request.getQuestionType()));
        question.setTopic(topic);
        question.setDifficultyLevel(DifficultyLevel.valueOf(request.getDifficultyLevel()));
        question.setIsQuestionBank(true);
        question.setAttachmentUrl(request.getAttachmentUrl());
        question.setIsQuestionBank(true); // Đánh dấu là question bank
        
        // Add options
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<QuestionOption> options = new ArrayList<>();
            for (QuestionBankRequest.QuestionOptionRequest optReq : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setOptionText(optReq.getOptionText());
                option.setIsCorrect(optReq.getIsCorrect());
                option.setOptionOrder(optReq.getOptionOrder());
                option.setQuestion(question);
                options.add(option);
            }
            question.setOptions(options);
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

    public Question getQuestion(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
    }
    
    // ========== QUESTION BANK METHODS ==========
    
    /**
     * Lấy tất cả câu hỏi trong question bank
     */
    public List<Question> getAllQuestionBank() {
        return questionRepository.findByIsQuestionBankTrueOrderByQuestionIdDesc();
    }
    
    /**
     * Lấy câu hỏi trong question bank theo topic
     */
    public List<Question> getQuestionBankByTopic(Long topicId) {
        // Validate topic exists
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        return questionRepository.findByTopic_TopicIdAndIsQuestionBankTrueOrderByQuestionIdDesc(topicId);
    }
    
    /**
     * Lấy câu hỏi trong question bank theo topic và độ khó
     */
    public List<Question> getQuestionBankByTopicAndDifficulty(Long topicId, DifficultyLevel difficultyLevel) {
        // Validate topic exists
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        return questionRepository.findByTopic_TopicIdAndDifficultyLevelAndIsQuestionBankTrue(
            topicId, difficultyLevel);
    }
    
    /**
     * Cập nhật câu hỏi trong question bank
     */
    @Transactional
    public Question updateQuestionBank(Long questionId, QuestionBankRequest request) {
        Question existing = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        
        // Validate là question bank
        if (existing.getIsQuestionBank() == null || !existing.getIsQuestionBank()) {
            throw new RuntimeException("This question is not a question bank question");
        }
        
        // Validate topic exists if changed
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + request.getTopicId()));
            existing.setTopic(topic);
        }
        
        // Update fields
        if (request.getQuestionText() != null) {
            existing.setQuestionText(request.getQuestionText());
        }
        if (request.getPoints() != null) {
            existing.setPoints(request.getPoints());
        }
        if (request.getQuestionType() != null) {
            existing.setQuestionType(QuestionType.valueOf(request.getQuestionType()));
        }
        if (request.getDifficultyLevel() != null) {
            existing.setDifficultyLevel(DifficultyLevel.valueOf(request.getDifficultyLevel()));
        }
        existing.setAttachmentUrl(request.getAttachmentUrl());
        
        // Update options
        if (request.getOptions() != null) {
            // Xóa options cũ
            existing.getOptions().clear();
            
            // Thêm options mới
            List<QuestionOption> newOptions = new ArrayList<>();
            for (QuestionBankRequest.QuestionOptionRequest optReq : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setOptionText(optReq.getOptionText());
                option.setIsCorrect(optReq.getIsCorrect());
                option.setOptionOrder(optReq.getOptionOrder());
                option.setQuestion(existing);
                newOptions.add(option);
            }
            existing.setOptions(newOptions);
        }
        
        return questionRepository.save(existing);
    }
    
    /**
     * Xóa câu hỏi từ question bank
     */
    @Transactional
    public void deleteQuestionBank(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        
        // Validate là question bank
        if (question.getIsQuestionBank() == null || !question.getIsQuestionBank()) {
            throw new RuntimeException("This question is not a question bank question");
        }
        
        questionRepository.deleteById(questionId);
    }
    
    /**
     * Đếm số câu hỏi trong question bank của một topic
     */
    public Long countQuestionBankByTopic(Long topicId) {
        return questionRepository.countQuestionsByTopicId(topicId);
    }
}