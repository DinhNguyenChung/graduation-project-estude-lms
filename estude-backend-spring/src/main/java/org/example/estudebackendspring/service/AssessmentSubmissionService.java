package org.example.estudebackendspring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.BadRequestException;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for assessment submission and grading
 */
@Slf4j
@Service
public class AssessmentSubmissionService {
    
    private final AssessmentSubmissionRepository submissionRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    
    public AssessmentSubmissionService(
            AssessmentSubmissionRepository submissionRepository,
            AssessmentAnswerRepository answerRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository) {
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
    }
    
    /**
     * Submit assessment answers and auto-grade
     * Returns detailed results with scoring and statistics
     */
    @Transactional
    public AssessmentSubmissionResponseDTO submitAssessment(SubmitAssessmentRequest request) {
        log.info("Submitting assessment: {} for student: {}", 
            request.getAssessmentId(), request.getStudentId());
        
        // Validate: Check if already submitted
        if (submissionRepository.existsByAssessmentIdAndStudent_UserId(
                request.getAssessmentId(), request.getStudentId())) {
            throw new DuplicateResourceException(
                "Assessment already submitted. Assessment ID: " + request.getAssessmentId());
        }
        
        // Fetch entities
        Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));
        
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.getSubjectId()));
        
        // Create submission entity
        AssessmentSubmission submission = new AssessmentSubmission();
        submission.setAssessmentId(request.getAssessmentId());
        submission.setStudent(student);
        submission.setSubject(subject);
        submission.setGradeLevel(request.getGradeLevel());
        submission.setDifficulty(request.getDifficulty());
        submission.setTotalQuestions(request.getAnswers().size());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setTimeTaken(request.getTimeTaken());
        
        // Process and grade each answer
        int correctCount = 0;
        List<AssessmentAnswer> answers = new ArrayList<>();
        Map<String, TopicStats> topicStatsMap = new HashMap<>();
        Map<String, DifficultyStats> difficultyStatsMap = new HashMap<>();
        
        for (SubmitAssessmentRequest.AssessmentAnswerRequest answerReq : request.getAnswers()) {
            // Fetch question
            Question question = questionRepository.findById(answerReq.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Question not found: " + answerReq.getQuestionId()));
            
            // Fetch chosen option
            QuestionOption chosenOption = questionOptionRepository.findById(answerReq.getChosenOptionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Option not found: " + answerReq.getChosenOptionId()));
            
            // Check if answer is correct
            boolean isCorrect = chosenOption.getIsCorrect();
            if (isCorrect) {
                correctCount++;
            }
            
            // Create answer entity
            AssessmentAnswer answer = new AssessmentAnswer();
            answer.setAssessmentSubmission(submission);
            answer.setQuestion(question);
            answer.setTopic(question.getTopic());
            answer.setChosenOption(chosenOption);
            answer.setIsCorrect(isCorrect);
            answer.setDifficultyLevel(question.getDifficultyLevel().name());
            answers.add(answer);
            
            // Update statistics
            if (question.getTopic() != null) {
                String topicName = question.getTopic().getName();
                TopicStats stats = topicStatsMap.computeIfAbsent(topicName, 
                    k -> new TopicStats(question.getTopic()));
                stats.totalQuestions++;
                if (isCorrect) stats.correctAnswers++;
            }
            
            String difficulty = question.getDifficultyLevel().name();
            DifficultyStats diffStats = difficultyStatsMap.computeIfAbsent(difficulty, 
                k -> new DifficultyStats(difficulty));
            diffStats.totalQuestions++;
            if (isCorrect) diffStats.correctAnswers++;
        }
        
        // Calculate score
        float score = (float) correctCount / request.getAnswers().size() * 100;
        submission.setCorrectAnswers(correctCount);
        submission.setScore(score);
        submission.setAnswers(answers);
        
        // Save submission
        AssessmentSubmission saved = submissionRepository.save(submission);
        log.info("Assessment submitted: submissionId={}, score={}", saved.getSubmissionId(), score);
        
        // Build response
        return buildSubmissionResponse(saved, topicStatsMap, difficultyStatsMap);
    }
    
    /**
     * Get all submissions for a student
     */
    @Transactional(readOnly = true)
    public List<AssessmentSubmissionSummaryDTO> getStudentSubmissions(Long studentId) {
        log.info("Getting all submissions for student: {}", studentId);
        
        List<AssessmentSubmission> submissions = submissionRepository
            .findByStudent_UserIdOrderBySubmittedAtDesc(studentId);
        
        return submissions.stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get submissions by student and subject
     */
    @Transactional(readOnly = true)
    public List<AssessmentSubmissionSummaryDTO> getStudentSubmissionsBySubject(
            Long studentId, Long subjectId) {
        log.info("Getting submissions for student: {} and subject: {}", studentId, subjectId);
        
        List<AssessmentSubmission> submissions = submissionRepository
            .findByStudent_UserIdAndSubject_SubjectIdOrderBySubmittedAtDesc(studentId, subjectId);
        
        return submissions.stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get detailed result of a specific submission
     */
    @Transactional(readOnly = true)
    public AssessmentSubmissionResponseDTO getSubmissionDetail(Long submissionId) {
        log.info("Getting submission detail: {}", submissionId);
        
        AssessmentSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));
        
        // Rebuild statistics
        Map<String, TopicStats> topicStatsMap = new HashMap<>();
        Map<String, DifficultyStats> difficultyStatsMap = new HashMap<>();
        
        for (AssessmentAnswer answer : submission.getAnswers()) {
            if (answer.getTopic() != null) {
                String topicName = answer.getTopic().getName();
                TopicStats stats = topicStatsMap.computeIfAbsent(topicName, 
                    k -> new TopicStats(answer.getTopic()));
                stats.totalQuestions++;
                if (answer.getIsCorrect()) stats.correctAnswers++;
            }
            
            String difficulty = answer.getDifficultyLevel();
            DifficultyStats diffStats = difficultyStatsMap.computeIfAbsent(difficulty, 
                k -> new DifficultyStats(difficulty));
            diffStats.totalQuestions++;
            if (answer.getIsCorrect()) diffStats.correctAnswers++;
        }
        
        return buildSubmissionResponse(submission, topicStatsMap, difficultyStatsMap);
    }
    
    /**
     * Build complete submission response with all details
     */
    private AssessmentSubmissionResponseDTO buildSubmissionResponse(
            AssessmentSubmission submission,
            Map<String, TopicStats> topicStatsMap,
            Map<String, DifficultyStats> difficultyStatsMap) {
        
        AssessmentSubmissionResponseDTO response = new AssessmentSubmissionResponseDTO();
        response.setSubmissionId(submission.getSubmissionId());
        response.setAssessmentId(submission.getAssessmentId());
        response.setStudentId(submission.getStudent().getUserId());
        response.setStudentName(submission.getStudent().getFullName());
        response.setSubjectId(submission.getSubject().getSubjectId());
        response.setSubjectName(submission.getSubject().getName());
        response.setGradeLevel(submission.getGradeLevel());
        response.setDifficulty(submission.getDifficulty());
        
        // Scoring
        response.setTotalQuestions(submission.getTotalQuestions());
        response.setCorrectAnswers(submission.getCorrectAnswers());
        response.setScore(submission.getScore());
        response.setPerformanceLevel(getPerformanceLevel(submission.getScore()));
        
        // Timing
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setTimeTaken(submission.getTimeTaken());
        
        // Detailed answers
        List<AssessmentSubmissionResponseDTO.AssessmentAnswerResultDTO> answerResults = 
            submission.getAnswers().stream()
                .map(this::convertToAnswerResultDTO)
                .collect(Collectors.toList());
        response.setAnswers(answerResults);
        
        // Statistics
        AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO statistics = 
            buildStatistics(topicStatsMap, difficultyStatsMap);
        response.setStatistics(statistics);
        
        return response;
    }
    
    /**
     * Convert answer entity to result DTO
     */
    private AssessmentSubmissionResponseDTO.AssessmentAnswerResultDTO convertToAnswerResultDTO(
            AssessmentAnswer answer) {
        
        AssessmentSubmissionResponseDTO.AssessmentAnswerResultDTO dto = 
            new AssessmentSubmissionResponseDTO.AssessmentAnswerResultDTO();
        
        Question question = answer.getQuestion();
        dto.setQuestionId(question.getQuestionId());
        dto.setQuestionText(question.getQuestionText());
        
        if (answer.getTopic() != null) {
            dto.setTopicId(answer.getTopic().getTopicId());
            dto.setTopicName(answer.getTopic().getName());
        }
        
        dto.setDifficultyLevel(answer.getDifficultyLevel());
        dto.setChosenOptionId(answer.getChosenOption().getOptionId());
        dto.setChosenOptionText(answer.getChosenOption().getOptionText());
        dto.setIsCorrect(answer.getIsCorrect());
        
        // Find correct option
        QuestionOption correctOption = question.getOptions().stream()
            .filter(QuestionOption::getIsCorrect)
            .findFirst()
            .orElse(null);
        
        if (correctOption != null) {
            dto.setCorrectOptionId(correctOption.getOptionId());
            dto.setCorrectOptionText(correctOption.getOptionText());
        }
        
        dto.setExplanation(null); // TODO: Add explanation field to Question entity
        
        return dto;
    }
    
    /**
     * Build statistics breakdown
     */
    private AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO buildStatistics(
            Map<String, TopicStats> topicStatsMap,
            Map<String, DifficultyStats> difficultyStatsMap) {
        
        AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO statistics = 
            new AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO();
        
        // Topic statistics
        Map<String, AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO.TopicStatDTO> topicStats = 
            topicStatsMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        TopicStats stats = entry.getValue();
                        float accuracy = (float) stats.correctAnswers / stats.totalQuestions * 100;
                        return new AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO.TopicStatDTO(
                            stats.topic.getTopicId(),
                            stats.topic.getName(),
                            stats.totalQuestions,
                            stats.correctAnswers,
                            accuracy
                        );
                    }
                ));
        statistics.setByTopic(topicStats);
        
        // Difficulty statistics
        Map<String, AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO.DifficultyStatDTO> diffStats = 
            difficultyStatsMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        DifficultyStats stats = entry.getValue();
                        float accuracy = (float) stats.correctAnswers / stats.totalQuestions * 100;
                        return new AssessmentSubmissionResponseDTO.AssessmentStatisticsDTO.DifficultyStatDTO(
                            stats.difficulty,
                            stats.totalQuestions,
                            stats.correctAnswers,
                            accuracy
                        );
                    }
                ));
        statistics.setByDifficulty(diffStats);
        
        return statistics;
    }
    
    /**
     * Get topic-wise statistics for a student
     * Shows accuracy percentage for each topic across all submissions
     * 
     * @param studentId Student's ID
     * @param subjectId Optional subject filter
     * @return List of topic statistics with accuracy percentages
     */
    public List<TopicStatisticsDTO> getStudentTopicStatistics(Long studentId, Long subjectId) {
        log.info("Getting topic statistics for student: {}, subject: {}", studentId, subjectId);
        
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        
        // Verify subject exists if provided
        if (subjectId != null && !subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
        }
        
        // Query statistics
        List<Object[]> results;
        if (subjectId != null) {
            results = answerRepository.findTopicStatisticsByStudentIdAndSubjectId(studentId, subjectId);
        } else {
            results = answerRepository.findTopicStatisticsByStudentId(studentId);
        }
        
        // Convert to DTOs
        List<TopicStatisticsDTO> statistics = new ArrayList<>();
        for (Object[] row : results) {
            String topicName = (String) row[0];
            Long totalQuestions = ((Number) row[1]).longValue();
            Long correctAnswers = ((Number) row[2]).longValue();
            Double accuracy = (Double) row[3];
            Long assessmentCount = ((Number) row[4]).longValue();
            
            TopicStatisticsDTO dto = new TopicStatisticsDTO();
            dto.setTopic(topicName);
            dto.setTotalQuestions(totalQuestions);
            dto.setCorrectAnswers(correctAnswers);
            dto.setAccuracy(accuracy);
            dto.setAssessmentCount(assessmentCount);
            
            statistics.add(dto);
        }
        
        log.info("Found {} topic statistics for student {}", statistics.size(), studentId);
        return statistics;
    }
    
    /**
     * Mark submission as improvement evaluated
     * Used by Layer 4 (Improvement Evaluation) to mark that this submission has been analyzed
     * 
     * @param submissionId Submission ID to mark
     * @return Updated submission entity
     */
    @Transactional
    public AssessmentSubmission markAsImprovementEvaluated(Long submissionId) {
        log.info("Marking submission {} as improvement evaluated", submissionId);
        
        AssessmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
        
        submission.setImprovementEvaluated(true);
        AssessmentSubmission updated = submissionRepository.save(submission);
        
        log.info("Submission {} marked as evaluated", submissionId);
        return updated;
    }
    
    /**
     * Reset improvement evaluated flag
     * Allows re-evaluation if needed
     * 
     * @param submissionId Submission ID to reset
     * @return Updated submission entity
     */
    @Transactional
    public AssessmentSubmission resetImprovementEvaluated(Long submissionId) {
        log.info("Resetting improvement evaluated flag for submission {}", submissionId);
        
        AssessmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
        
        submission.setImprovementEvaluated(false);
        AssessmentSubmission updated = submissionRepository.save(submission);
        
        log.info("Submission {} flag reset", submissionId);
        return updated;
    }
    
    /**
     * Convert submission to summary DTO
     */
    private AssessmentSubmissionSummaryDTO convertToSummaryDTO(AssessmentSubmission submission) {
        AssessmentSubmissionSummaryDTO dto = new AssessmentSubmissionSummaryDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setAssessmentId(submission.getAssessmentId());
        dto.setSubjectId(submission.getSubject().getSubjectId());
        dto.setSubjectName(submission.getSubject().getName());
        dto.setGradeLevel(submission.getGradeLevel());
        dto.setDifficulty(submission.getDifficulty());
        dto.setImprovementEvaluated(submission.getImprovementEvaluated());
        dto.setTotalQuestions(submission.getTotalQuestions());
        dto.setCorrectAnswers(submission.getCorrectAnswers());
        dto.setScore(submission.getScore());
        dto.setPerformanceLevel(getPerformanceLevel(submission.getScore()));
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setTimeTaken(submission.getTimeTaken());
        return dto;
    }
    
    /**
     * Determine performance level based on score
     */
    private String getPerformanceLevel(float score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 50) return "AVERAGE";
        return "NEEDS_IMPROVEMENT";
    }
    
    // Helper classes for statistics
    private static class TopicStats {
        Topic topic;
        int totalQuestions = 0;
        int correctAnswers = 0;
        
        TopicStats(Topic topic) {
            this.topic = topic;
        }
    }
    
    private static class DifficultyStats {
        String difficulty;
        int totalQuestions = 0;
        int correctAnswers = 0;
        
        DifficultyStats(String difficulty) {
            this.difficulty = difficulty;
        }
    }
}
