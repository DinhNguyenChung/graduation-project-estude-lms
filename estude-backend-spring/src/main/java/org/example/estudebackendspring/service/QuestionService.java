package org.example.estudebackendspring.service;


import org.example.estudebackendspring.dto.PageResponse;
import org.example.estudebackendspring.dto.QuestionBankDTO;
import org.example.estudebackendspring.dto.QuestionBankRequest;
import org.example.estudebackendspring.dto.QuestionBankSummaryDTO;
import org.example.estudebackendspring.dto.QuestionOptionDTO;
import org.example.estudebackendspring.dto.QuestionResponseDTO;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.QuestionType;
import org.example.estudebackendspring.mapper.QuestionBankMapper;
import org.example.estudebackendspring.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;
    private final TopicRepository topicRepository;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionOptionRepository questionOptionRepository;

    public QuestionService(QuestionRepository questionRepository, 
                          AssignmentRepository assignmentRepository,
                          TopicRepository topicRepository,
                          QuestionBankMapper questionBankMapper,
                          QuestionOptionRepository questionOptionRepository) {
        this.questionRepository = questionRepository;
        this.assignmentRepository = assignmentRepository;
        this.topicRepository = topicRepository;
        this.questionBankMapper = questionBankMapper;
        this.questionOptionRepository = questionOptionRepository;
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
     * Clear cache khi create
     */
    @Transactional
    @CacheEvict(value = {"questionBankSummary", "questionBankFull", "questionBankByTopic", 
                          "questionBankBySubject", "questionBankBySubjectGrade", "questionBankByGrade"}, allEntries = true)
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
    
    // ========== QUESTION BANK METHODS - OPTIMIZED ==========
    
    /**
     * Lấy tất cả câu hỏi trong question bank với pagination và summary
     * Tối ưu cho performance với projection query
     * 
     * @param page số trang (0-indexed)
     * @param size số lượng items per page
     * @param sortBy trường để sort (mặc định: questionId)
     * @param direction hướng sort (ASC/DESC, mặc định: DESC)
     * @return PageResponse chứa QuestionBankSummaryDTO
     */
    @Cacheable(value = "questionBankSummary", key = "#page + '-' + #size + '-' + #sortBy + '-' + #direction")
    public PageResponse<QuestionBankSummaryDTO> getAllQuestionBankSummary(
            int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<QuestionBankSummaryDTO> questionPage = questionRepository.findAllQuestionBankSummary(pageable);
        return PageResponse.of(questionPage);
    }
    
    /**
     * Lấy tất cả câu hỏi trong question bank với full details
     * Chỉ dùng khi cần đầy đủ thông tin (ví dụ: export)
     */
    @Cacheable(value = "questionBankFull", key = "#page + '-' + #size")
    public PageResponse<QuestionBankDTO> getAllQuestionBankFull(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questionId"));
        Page<Question> questionPage = questionRepository.findAllQuestionBankWithDetails(pageable);
        
        Page<QuestionBankDTO> dtoPage = questionPage.map(questionBankMapper::toDTO);
        return PageResponse.of(dtoPage);
    }
    
    /**
     * Lấy câu hỏi trong question bank theo topic với pagination
     */
    @Cacheable(value = "questionBankByTopic", key = "#topicId + '-' + #page + '-' + #size")
    public PageResponse<QuestionBankSummaryDTO> getQuestionBankByTopicSummary(
            Long topicId, int page, int size) {
        // Validate topic exists
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questionId"));
        Page<QuestionBankSummaryDTO> questionPage = questionRepository.findQuestionBankByTopicSummary(topicId, pageable);
        return PageResponse.of(questionPage);
    }
    
    /**
     * Lấy câu hỏi trong question bank theo topic và độ khó với pagination
     */
    public PageResponse<QuestionBankDTO> getQuestionBankByTopicAndDifficulty(
            Long topicId, DifficultyLevel difficultyLevel, int page, int size) {
        // Validate topic exists
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questionId"));
        Page<Question> questionPage = questionRepository.findQuestionBankByTopicAndDifficultyWithDetails(
                topicId, difficultyLevel, pageable);
        
        Page<QuestionBankDTO> dtoPage = questionPage.map(questionBankMapper::toDTO);
        return PageResponse.of(dtoPage);
    }
    
    /**
     * Lấy một question với đầy đủ chi tiết
     */
    @Cacheable(value = "questionDetail", key = "#questionId")
    public QuestionBankDTO getQuestionBankDetail(Long questionId) {
        Question question = questionRepository.findByIdWithDetails(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        
        // Validate là question bank
        if (question.getIsQuestionBank() == null || !question.getIsQuestionBank()) {
            throw new RuntimeException("This question is not a question bank question");
        }
        
        return questionBankMapper.toDTO(question);
    }
    
    // ========== LEGACY METHODS (backward compatibility) ==========
    
    /**
     * @deprecated Use getAllQuestionBankSummary() instead
     */
    @Deprecated
    public List<Question> getAllQuestionBank() {
        return questionRepository.findByIsQuestionBankTrueOrderByQuestionIdDesc();
    }
    
    /**
     * @deprecated Use getQuestionBankByTopicSummary() instead
     */
    @Deprecated
    public List<Question> getQuestionBankByTopic(Long topicId) {
        // Validate topic exists
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        return questionRepository.findByTopic_TopicIdAndIsQuestionBankTrueOrderByQuestionIdDesc(topicId);
    }
    
    /**
     * @deprecated Use getQuestionBankByTopicAndDifficulty() with pagination instead
     */
    @Deprecated
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
     * Clear cache khi update
     */
    @Transactional
    @CacheEvict(value = {"questionBankSummary", "questionBankFull", "questionBankByTopic", "questionDetail",
                          "questionBankBySubject", "questionBankBySubjectGrade", "questionBankByGrade"}, allEntries = true)
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
     * Clear cache khi delete
     */
    @Transactional
    @CacheEvict(value = {"questionBankSummary", "questionBankFull", "questionBankByTopic", "questionDetail",
                          "questionBankBySubject", "questionBankBySubjectGrade", "questionBankByGrade"}, allEntries = true)
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
    
    // ========== FILTER BY SUBJECT AND GRADE ==========
    
    /**
     * Lấy câu hỏi trong question bank theo subject với pagination
     * Hỗ trợ filter theo topicId (optional)
     */
    @Cacheable(value = "questionBankBySubject", key = "#subjectId + '-' + #topicId + '-' + #page + '-' + #size")
    public PageResponse<QuestionBankSummaryDTO> getQuestionBankBySubjectSummary(
            Long subjectId, Long topicId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questionId"));
        Page<QuestionBankSummaryDTO> questionPage = questionRepository.findQuestionBankBySubjectAndTopicSummary(
                subjectId, topicId, pageable);
        
        // Populate question options
        populateQuestionOptions(questionPage.getContent());
        
        return PageResponse.of(questionPage);
    }
    
    /**
     * Lấy câu hỏi trong question bank theo subject và grade level với pagination
     * @param gradeLevel GradeLevel enum (GRADE_10, GRADE_11, etc.)
     */
    @Cacheable(value = "questionBankBySubjectGrade", key = "#subjectId + '-' + #gradeLevel + '-' + #page + '-' + #size")
    public PageResponse<QuestionBankSummaryDTO> getQuestionBankBySubjectAndGradeLevelSummary(
            Long subjectId, org.example.estudebackendspring.enums.GradeLevel gradeLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questionId"));
        Page<QuestionBankSummaryDTO> questionPage = questionRepository.findQuestionBankBySubjectAndGradeLevelSummary(
                subjectId, gradeLevel, pageable);
        return PageResponse.of(questionPage);
    }
    
    /**
     * Lấy câu hỏi trong question bank theo grade level với pagination
     * @param gradeLevel GradeLevel enum (GRADE_10, GRADE_11, etc.)
     */
    @Cacheable(value = "questionBankByGrade", key = "#gradeLevel + '-' + #page + '-' + #size")
    public PageResponse<QuestionBankSummaryDTO> getQuestionBankByGradeLevelSummary(
            org.example.estudebackendspring.enums.GradeLevel gradeLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questionId"));
        Page<QuestionBankSummaryDTO> questionPage = questionRepository.findQuestionBankByGradeLevelSummary(gradeLevel, pageable);
        return PageResponse.of(questionPage);
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Populate question options cho danh sách QuestionBankSummaryDTO
     * Sử dụng batch query để tránh N+1 problem
     */
    private void populateQuestionOptions(List<QuestionBankSummaryDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        
        // Lấy danh sách questionId
        List<Long> questionIds = questions.stream()
                .map(QuestionBankSummaryDTO::getQuestionId)
                .collect(Collectors.toList());
        
        // Batch query để lấy tất cả options
        List<QuestionOption> allOptions = questionOptionRepository.findByQuestionIdIn(questionIds);
        
        // Group options theo questionId
        Map<Long, List<QuestionOptionDTO>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(
                        option -> option.getQuestion().getQuestionId(),
                        Collectors.mapping(
                                option -> new QuestionOptionDTO(
                                        option.getOptionId(),
                                        option.getOptionText(),
                                        option.getOptionOrder(),
                                        option.getIsCorrect()
                                ),
                                Collectors.toList()
                        )
                ));
        
        // Gán options vào từng question
        questions.forEach(question -> {
            List<QuestionOptionDTO> options = optionsByQuestionId.getOrDefault(
                    question.getQuestionId(), 
                    new ArrayList<>()
            );
            question.setQuestionOptions(options);
        });
    }
    
    /**
     * Convert Question entity to DTO to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public QuestionResponseDTO convertToDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setQuestionText(question.getQuestionText());
        dto.setPoints(question.getPoints());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setAttachmentUrl(question.getAttachmentUrl());
        dto.setDifficultyLevel(question.getDifficultyLevel());
        dto.setIsQuestionBank(question.getIsQuestionBank());
        
        // Assignment info
        if (question.getAssignment() != null) {
            dto.setAssignmentId(question.getAssignment().getAssignmentId());
            dto.setAssignmentTitle(question.getAssignment().getTitle());
        }
        
        // Topic info
        if (question.getTopic() != null) {
            dto.setTopicId(question.getTopic().getTopicId());
            dto.setTopicName(question.getTopic().getName());
        }
        
        // Options
        if (question.getOptions() != null) {
            List<QuestionOptionDTO> optionDTOs = question.getOptions().stream()
                .map(option -> new QuestionOptionDTO(
                    option.getOptionId(),
                    option.getOptionText(),
                    option.getOptionOrder(),
                    option.getIsCorrect()
                ))
                .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }
        
        return dto;
    }
}