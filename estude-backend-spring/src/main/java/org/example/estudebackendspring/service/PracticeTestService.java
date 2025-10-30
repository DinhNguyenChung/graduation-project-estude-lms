package org.example.estudebackendspring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.enums.SubmissionStatus;
import org.example.estudebackendspring.enums.TestType;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PracticeTestService {
    
    private final PracticeTestRepository practiceTestRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final SubmissionRepository submissionRepository;
    private final AnswerRepository answerRepository;
    private final TopicProgressRepository topicProgressRepository;
    private final QuestionOptionRepository questionOptionRepository;
    
    public PracticeTestService(PracticeTestRepository practiceTestRepository,
                              TopicRepository topicRepository,
                              QuestionRepository questionRepository,
                              StudentRepository studentRepository,
                              SubjectRepository subjectRepository,
                              SubmissionRepository submissionRepository,
                              AnswerRepository answerRepository,
                              TopicProgressRepository topicProgressRepository,
                              QuestionOptionRepository questionOptionRepository) {
        this.practiceTestRepository = practiceTestRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.topicProgressRepository = topicProgressRepository;
        this.questionOptionRepository = questionOptionRepository;
    }
    
    /**
     * Tạo practice test mới
     */
    @Transactional
    public PracticeTestDTO createPracticeTest(CreatePracticeTestRequest request) {
        log.info("Creating practice test for student: {}, subject: {}, topics: {}", 
            request.getStudentId(), request.getSubjectId(), request.getTopicIds());
        
        // Validate
        if (request.getTopicIds() == null || request.getTopicIds().isEmpty()) {
            throw new IllegalArgumentException("At least one topic is required");
        }
        if (request.getNumQuestions() <= request.getTopicIds().size()) {
            throw new IllegalArgumentException("Number of questions must be greater than number of topics");
        }
        
        Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));
        
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.getSubjectId()));
        
        List<Topic> topics = topicRepository.findAllById(request.getTopicIds());
        if (topics.size() != request.getTopicIds().size()) {
            throw new ResourceNotFoundException("Some topics not found");
        }
        
        // Random câu hỏi từ question bank
        List<Question> selectedQuestions = selectRandomQuestions(
            topics, 
            request.getNumQuestions(), 
            request.getDifficultyLevel()
        );
        
        if (selectedQuestions.size() < request.getNumQuestions()) {
            log.warn("Not enough questions. Requested: {}, Found: {}", 
                request.getNumQuestions(), selectedQuestions.size());
        }
        
        // Tạo practice test
        PracticeTest test = new PracticeTest();
        test.setTitle(generateTitle(subject, topics));
        test.setSubject(subject);
        test.setStudent(student);
        test.setTestType(TestType.SELF_ASSESSMENT);
        test.setCreatedAt(LocalDateTime.now());
        test.setTotalQuestions(selectedQuestions.size());
        test.setTimeLimit(request.getTimeLimit());
        test.setSelectedTopics(topics);
        test.setQuestions(selectedQuestions);
        
        PracticeTest saved = practiceTestRepository.save(test);
        log.info("Practice test created: {}", saved.getTestId());
        
        return convertToDTO(saved);
    }
    
    /**
     * Random câu hỏi từ question bank theo topics và difficulty
     */
    private List<Question> selectRandomQuestions(List<Topic> topics, int numQuestions, String difficultyLevel) {
        List<Question> allQuestions = new ArrayList<>();
        
        // Lấy câu hỏi từ mỗi topic
        for (Topic topic : topics) {
            List<Question> topicQuestions = topic.getQuestions().stream()
                .filter(q -> q.getIsQuestionBank() != null && q.getIsQuestionBank())
                .collect(Collectors.toList());
            
            // Filter by difficulty nếu cần
            if (difficultyLevel != null && !difficultyLevel.equalsIgnoreCase("MIXED")) {
                try {
                    DifficultyLevel level = DifficultyLevel.valueOf(difficultyLevel.toUpperCase());
                    topicQuestions = topicQuestions.stream()
                        .filter(q -> q.getDifficultyLevel() == level)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid difficulty level: {}", difficultyLevel);
                }
            }
            
            allQuestions.addAll(topicQuestions);
        }
        
        // Shuffle và chọn số lượng cần thiết
        Collections.shuffle(allQuestions);
        
        int actualNum = Math.min(numQuestions, allQuestions.size());
        return allQuestions.subList(0, actualNum);
    }
    
    /**
     * Generate title for practice test
     */
    private String generateTitle(Subject subject, List<Topic> topics) {
        if (topics.size() == 1) {
            return String.format("Kiểm tra %s - %s", subject.getName(), topics.get(0).getName());
        } else {
            return String.format("Kiểm tra %s - %d chủ đề", subject.getName(), topics.size());
        }
    }
    
    /**
     * Lấy practice test detail
     */
    public PracticeTestDTO getPracticeTest(Long testId) {
        log.info("Getting practice test: {}", testId);
        PracticeTest test = practiceTestRepository.findById(testId)
            .orElseThrow(() -> new ResourceNotFoundException("Practice test not found: " + testId));
        return convertToDTO(test);
    }
    
    /**
     * Lấy danh sách practice tests của học sinh
     */
    public List<PracticeTestDTO> getStudentPracticeTests(Long studentId, Boolean completed) {
        log.info("Getting practice tests for student: {}, completed: {}", studentId, completed);
        
        List<PracticeTest> tests;
        if (completed == null) {
            tests = practiceTestRepository.findByStudent_UserIdOrderByCreatedAtDesc(studentId);
        } else if (completed) {
            tests = practiceTestRepository.findCompletedTestsByStudent(studentId);
        } else {
            tests = practiceTestRepository.findPendingTestsByStudent(studentId);
        }
        
        return tests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Nộp bài practice test
     */
    @Transactional
    public SubmissionWithTopicsDTO submitPracticeTest(Long testId, SubmissionRequest request) {
        log.info("Submitting practice test: {}", testId);
        
        PracticeTest test = practiceTestRepository.findById(testId)
            .orElseThrow(() -> new ResourceNotFoundException("Practice test not found: " + testId));
        
        if (test.getSubmission() != null) {
            throw new IllegalStateException("Practice test already submitted");
        }
        
        Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));
        
        // Tạo submission
        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setPracticeTest(test);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setAttemptNumber(1);
        
        submission = submissionRepository.save(submission);
        
        // Process answers
        Map<Long, TopicStats> topicStatsMap = new HashMap<>();
        float totalScore = 0f;
        int totalCorrect = 0;
        
        for (AnswerRequest ar : request.getAnswers()) {
            Question question = questionRepository.findById(ar.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + ar.getQuestionId()));
            
            Answer answer = new Answer();
            answer.setSubmission(submission);
            answer.setQuestion(question);
            answer.setStudentAnswerText(ar.getTextAnswer());
            
            // Auto-grade cho multiple choice
            boolean isCorrect = false;
            if (ar.getChosenOptionId() != null) {
                QuestionOption chosen = questionOptionRepository.findById(ar.getChosenOptionId()).orElse(null);
                if (chosen != null) {
                    answer.setChosenOption(chosen);
                    isCorrect = chosen.getIsCorrect();
                }
            }
            
            answer.setIsCorrect(isCorrect);
            answer.setScore(isCorrect ? question.getPoints() : 0f);
            answerRepository.save(answer);
            
            if (isCorrect) {
                totalScore += question.getPoints();
                totalCorrect++;
            }
            
            // Track by topic
            if (question.getTopic() != null) {
                Long topicId = question.getTopic().getTopicId();
                TopicStats stats = topicStatsMap.computeIfAbsent(topicId, 
                    k -> new TopicStats(question.getTopic()));
                stats.totalQuestions++;
                if (isCorrect) stats.correctAnswers++;
            }
        }
        
        // Save topic progress
        for (TopicStats stats : topicStatsMap.values()) {
            TopicProgress progress = new TopicProgress();
            progress.setStudent(student);
            progress.setTopic(stats.topic);
            progress.setSubmission(submission);
            progress.setTotalQuestions(stats.totalQuestions);
            progress.setCorrectAnswers(stats.correctAnswers);
            progress.setAccuracyRate((float) stats.correctAnswers / stats.totalQuestions);
            progress.setRecordedAt(LocalDateTime.now());
            topicProgressRepository.save(progress);
        }
        
        // Return result
        return buildSubmissionWithTopics(submission, topicStatsMap, totalScore, totalCorrect);
    }
    
    /**
     * Convert to DTO
     */
    private PracticeTestDTO convertToDTO(PracticeTest test) {
        PracticeTestDTO dto = new PracticeTestDTO();
        dto.setTestId(test.getTestId());
        dto.setTitle(test.getTitle());
        dto.setTestType(test.getTestType().name());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setExpiresAt(test.getExpiresAt());
        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setTimeLimit(test.getTimeLimit());
        
        if (test.getSubject() != null) {
            dto.setSubjectId(test.getSubject().getSubjectId());
            dto.setSubjectName(test.getSubject().getName());
        }
        
        if (test.getStudent() != null) {
            dto.setStudentId(test.getStudent().getUserId());
            dto.setStudentName(test.getStudent().getFullName());
        }
        
        dto.setIsCompleted(test.getSubmission() != null);
        if (test.getSubmission() != null) {
            dto.setSubmissionId(test.getSubmission().getSubmissionId());
        }
        
        // Topics
        List<TopicDTO> topicDTOs = test.getSelectedTopics().stream()
            .map(this::convertTopicToDTO)
            .collect(Collectors.toList());
        dto.setSelectedTopics(topicDTOs);
        
        // Questions (chỉ khi chưa làm hoặc cần xem lại)
        List<QuestionDTO> questionDTOs = test.getQuestions().stream()
            .map(this::convertQuestionToDTO)
            .collect(Collectors.toList());
        dto.setQuestions(questionDTOs);
        
        return dto;
    }
    
    private TopicDTO convertTopicToDTO(Topic topic) {
        TopicDTO dto = new TopicDTO();
        dto.setTopicId(topic.getTopicId());
        dto.setName(topic.getName());
        dto.setDescription(topic.getDescription());
        dto.setChapter(topic.getChapter());
        dto.setOrderIndex(topic.getOrderIndex());
        return dto;
    }
    
    private QuestionDTO convertQuestionToDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionId(q.getQuestionId());
        dto.setQuestionText(q.getQuestionText());
        dto.setPoints(q.getPoints());
        dto.setQuestionType(q.getQuestionType() != null ? q.getQuestionType().name() : null);
        dto.setQuestionOrder(q.getQuestionOrder());
        dto.setAttachmentUrl(q.getAttachmentUrl());
        
        List<QuestionOptionDTO> options = q.getOptions().stream()
            .map(opt -> {
                QuestionOptionDTO optDto = new QuestionOptionDTO();
                optDto.setOptionId(opt.getOptionId());
                optDto.setOptionText(opt.getOptionText());
                optDto.setOptionOrder(opt.getOptionOrder());
                // Don't include isCorrect here for security
                return optDto;
            })
            .collect(Collectors.toList());
        dto.setOptions(options);
        
        return dto;
    }
    
    private SubmissionWithTopicsDTO buildSubmissionWithTopics(
            Submission submission, Map<Long, TopicStats> topicStatsMap, 
            float totalScore, int totalCorrect) {
        
        SubmissionWithTopicsDTO dto = new SubmissionWithTopicsDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setTotalScore(totalScore);
        dto.setTotalQuestions(submission.getAnswers().size());
        dto.setCorrectAnswers(totalCorrect);
        dto.setOverallAccuracy((float) totalCorrect / submission.getAnswers().size());
        
        List<SubmissionWithTopicsDTO.TopicResultDTO> topicResults = topicStatsMap.values().stream()
            .map(stats -> {
                SubmissionWithTopicsDTO.TopicResultDTO result = 
                    new SubmissionWithTopicsDTO.TopicResultDTO();
                result.setTopicId(stats.topic.getTopicId());
                result.setTopicName(stats.topic.getName());
                result.setTotalQuestions(stats.totalQuestions);
                result.setCorrectAnswers(stats.correctAnswers);
                
                float accuracy = (float) stats.correctAnswers / stats.totalQuestions;
                result.setAccuracyRate(accuracy);
                result.setStatus(getAccuracyStatus(accuracy));
                
                return result;
            })
            .collect(Collectors.toList());
        
        dto.setTopicResults(topicResults);
        return dto;
    }
    
    private String getAccuracyStatus(float accuracy) {
        if (accuracy >= 0.9f) return "EXCELLENT";
        if (accuracy >= 0.7f) return "GOOD";
        if (accuracy >= 0.5f) return "NEED_IMPROVEMENT";
        return "WEAK";
    }
    
    // Helper class
    private static class TopicStats {
        Topic topic;
        int totalQuestions = 0;
        int correctAnswers = 0;
        
        TopicStats(Topic topic) {
            this.topic = topic;
        }
    }
    
    /**
     * Xóa practice test (chỉ nếu chưa làm)
     */
    @Transactional
    public void deletePracticeTest(Long testId) {
        log.info("Deleting practice test: {}", testId);
        
        PracticeTest test = practiceTestRepository.findById(testId)
            .orElseThrow(() -> new ResourceNotFoundException("Practice test not found: " + testId));
        
        if (test.getSubmission() != null) {
            throw new IllegalStateException("Cannot delete completed practice test");
        }
        
        practiceTestRepository.delete(test);
        log.info("Practice test deleted: {}", testId);
    }
}
