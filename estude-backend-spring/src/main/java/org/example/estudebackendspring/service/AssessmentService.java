package org.example.estudebackendspring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.Question;
import org.example.estudebackendspring.entity.QuestionOption;
import org.example.estudebackendspring.entity.Subject;
import org.example.estudebackendspring.entity.Topic;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.example.estudebackendspring.exception.InsufficientQuestionsException;
import org.example.estudebackendspring.exception.InvalidQuestionCountException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.QuestionRepository;
import org.example.estudebackendspring.repository.SubjectRepository;
import org.example.estudebackendspring.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating assessment questions from Question Bank
 * Implements distribution algorithm as per ASSESSMENT_API_DOCS.md
 */
@Slf4j
@Service
public class AssessmentService {
    
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    
    public AssessmentService(QuestionRepository questionRepository,
                           TopicRepository topicRepository,
                           SubjectRepository subjectRepository) {
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
    }
    
    /**
     * Generate assessment questions based on request
     * Algorithm:
     * 1. Validate: numQuestions >= topicIds.length
     * 2. Distribute questions evenly across topics
     * 3. For each topic, select questions by difficulty (40-40-20 for mixed)
     * 4. Shuffle all questions
     * 5. Return response with distribution stats
     */
    @Transactional(readOnly = true)
    public AssessmentResponseDTO generateAssessmentQuestions(CreateAssessmentRequest request) {
        log.info("Generating assessment: {} questions from {} topics with difficulty: {}", 
            request.getNumQuestions(), request.getTopicIds().size(), request.getDifficulty());
        
        // Step 1: Validation
        validateRequest(request);
        
        // Step 2: Fetch entities
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.getSubjectId()));
        
        List<Topic> topics = topicRepository.findAllById(request.getTopicIds());
        if (topics.size() != request.getTopicIds().size()) {
            throw new ResourceNotFoundException("Some topics not found");
        }
        
        // Step 3: Distribute questions across topics
        Map<Long, Integer> topicQuestionCounts = distributeQuestionsAcrossTopics(
            request.getTopicIds(), request.getNumQuestions());
        
        // Step 4: Select questions for each topic
        List<Question> allQuestions = new ArrayList<>();
        Map<String, Integer> topicDistribution = new HashMap<>();
        Map<String, Integer> difficultyDistribution = new HashMap<>();
        difficultyDistribution.put("EASY", 0);
        difficultyDistribution.put("MEDIUM", 0);
        difficultyDistribution.put("HARD", 0);
        
        for (Topic topic : topics) {
            int questionCount = topicQuestionCounts.get(topic.getTopicId());
            
            List<Question> topicQuestions = selectQuestionsForTopic(
                topic, questionCount, request.getDifficulty());
            
            allQuestions.addAll(topicQuestions);
            topicDistribution.put(topic.getTopicId().toString(), topicQuestions.size());
            
            // Update difficulty distribution
            for (Question q : topicQuestions) {
                String difficulty = q.getDifficultyLevel().name();
                difficultyDistribution.put(difficulty, difficultyDistribution.get(difficulty) + 1);
            }
        }
        
        // Step 5: Shuffle questions for randomness
        Collections.shuffle(allQuestions);
        
        // Step 6: Build response
        return buildAssessmentResponse(allQuestions, subject, request.getDifficulty(), 
            topicDistribution, difficultyDistribution);
    }
    
    /**
     * Validate request according to docs requirements
     */
    private void validateRequest(CreateAssessmentRequest request) {
        int numQuestions = request.getNumQuestions();
        int numTopics = request.getTopicIds().size();
        
        // Rule 1: Số câu hỏi phải >= số topics
        if (numQuestions < numTopics) {
            throw new InvalidQuestionCountException(
                numQuestions, numTopics);
        }
        
        // Rule 2: Với mixed difficulty, mỗi topic cần ít nhất 2 câu để đảm bảo tỷ lệ
        if ("mixed".equalsIgnoreCase(request.getDifficulty())) {
            int minQuestionsPerTopic = 2;
            int recommendedMinTotal = numTopics * minQuestionsPerTopic;
            
            if (numQuestions < recommendedMinTotal) {
                throw new InvalidQuestionCountException(
                    String.format(
                        "Với mixed difficulty và %d topics, cần ít nhất %d câu hỏi (tối thiểu %d câu/topic) để đảm bảo tỷ lệ độ khó 40-40-20. " +
                        "Hiện tại bạn chỉ yêu cầu %d câu.",
                        numTopics, recommendedMinTotal, minQuestionsPerTopic, numQuestions
                    )
                );
            }
        }
    }
    
    /**
     * Distribute questions evenly across topics
     * Algorithm:
     * - Each topic gets baseCount = floor(total / topicCount)
     * - Remaining questions distributed randomly
     */
    private Map<Long, Integer> distributeQuestionsAcrossTopics(List<Long> topicIds, int totalQuestions) {
        Map<Long, Integer> distribution = new HashMap<>();
        
        int baseCount = totalQuestions / topicIds.size();
        int remainder = totalQuestions % topicIds.size();
        
        // Give base count to all topics
        for (Long topicId : topicIds) {
            distribution.put(topicId, baseCount);
        }
        
        // Distribute remainder randomly
        List<Long> shuffledTopics = new ArrayList<>(topicIds);
        Collections.shuffle(shuffledTopics);
        
        for (int i = 0; i < remainder; i++) {
            Long topicId = shuffledTopics.get(i);
            distribution.put(topicId, distribution.get(topicId) + 1);
        }
        
        log.info("Question distribution: {}", distribution);
        return distribution;
    }
    
    /**
     * Select questions for a specific topic based on difficulty
     * For mixed: 40% EASY, 40% MEDIUM, 20% HARD
     * Implements fallback if not enough questions of specific difficulty
     */
    private List<Question> selectQuestionsForTopic(Topic topic, int count, String difficultyMode) {
        log.info("Selecting {} questions for topic {} ({})", count, topic.getTopicId(), topic.getName());
        
        if (difficultyMode.equals("mixed")) {
            return selectMixedDifficultyQuestions(topic, count);
        } else {
            return selectSingleDifficultyQuestions(topic, count, difficultyMode);
        }
    }
    
    /**
     * Select questions with mixed difficulty (40-40-20 ratio)
     */
    private List<Question> selectMixedDifficultyQuestions(Topic topic, int count) {
        // FIX: Đảm bảo không có giá trị âm
        // Sử dụng Math.round() thay vì Math.ceil() để tránh tổng vượt quá count
        int easyCount = Math.round(count * 0.4f);      // 40%
        int mediumCount = Math.round(count * 0.4f);    // 40%
        int hardCount = count - easyCount - mediumCount; // 20% (còn lại)
        
        // Đảm bảo hardCount không âm (có thể xảy ra với số câu nhỏ)
        if (hardCount < 0) {
            mediumCount += hardCount; // Giảm medium
            hardCount = 0;
        }
        
        log.info("Mixed distribution for topic {} (ID={}): EASY={}, MEDIUM={}, HARD={} (Total={})", 
            topic.getName(), topic.getTopicId(), easyCount, mediumCount, hardCount, count);
        
        List<Question> questions = new ArrayList<>();
        
        try {
            // Fetch EASY questions
            if (easyCount > 0) {
                log.debug("Fetching {} EASY questions for topic {}", easyCount, topic.getName());
                List<Question> easyQuestions = getRandomQuestionsWithFallback(
                    topic, DifficultyLevel.EASY, easyCount);
                questions.addAll(easyQuestions);
                log.debug("Successfully fetched {} EASY questions", easyQuestions.size());
            }
            
            // Fetch MEDIUM questions
            if (mediumCount > 0) {
                log.debug("Fetching {} MEDIUM questions for topic {}", mediumCount, topic.getName());
                List<Question> mediumQuestions = getRandomQuestionsWithFallback(
                    topic, DifficultyLevel.MEDIUM, mediumCount);
                questions.addAll(mediumQuestions);
                log.debug("Successfully fetched {} MEDIUM questions", mediumQuestions.size());
            }
            
            // Fetch HARD questions
            if (hardCount > 0) {
                log.debug("Fetching {} HARD questions for topic {}", hardCount, topic.getName());
                List<Question> hardQuestions = getRandomQuestionsWithFallback(
                    topic, DifficultyLevel.HARD, hardCount);
                questions.addAll(hardQuestions);
                log.debug("Successfully fetched {} HARD questions", hardQuestions.size());
            }
            
        } catch (Exception e) {
            log.error("Error fetching questions for topic {} (ID={}): {}", 
                topic.getName(), topic.getTopicId(), e.getMessage(), e);
            throw e;
        }
        
        log.info("Total questions selected for topic {}: {}", topic.getName(), questions.size());
        return questions;
    }
    
    /**
     * Select questions of single difficulty level
     */
    private List<Question> selectSingleDifficultyQuestions(Topic topic, int count, String difficultyMode) {
        DifficultyLevel level = mapDifficultyLevel(difficultyMode);
        return getRandomQuestionsWithFallback(topic, level, count);
    }
    
    /**
     * Get random questions with fallback to other difficulties if needed
     * Implements fallback strategy from docs
     */
    private List<Question> getRandomQuestionsWithFallback(Topic topic, DifficultyLevel difficulty, int count) {
        log.debug("Requesting {} {} questions for topic {} (ID={})", 
            count, difficulty, topic.getName(), topic.getTopicId());
        
        // First, try to get questions of requested difficulty
        List<Question> questions = questionRepository.findByTopic_TopicIdAndDifficultyLevelAndIsQuestionBankTrue(
            topic.getTopicId(), difficulty);
        
        log.info("Found {} {} questions for topic {} (ID={}) [requested: {}]", 
            questions.size(), difficulty, topic.getName(), topic.getTopicId(), count);
        
        // Check if list is empty
        if (questions.isEmpty()) {
            log.warn("⚠️ NO {} questions found for topic {} (ID={}). Starting fallback...", 
                difficulty, topic.getName(), topic.getTopicId());
        }
        
        if (questions.size() >= count) {
            // Enough questions, shuffle and return
            Collections.shuffle(questions);
            log.debug("✅ Sufficient questions available. Returning {} out of {} available", 
                count, questions.size());
            return questions.subList(0, count);
        }
        
        // Not enough questions, implement fallback
        log.warn("⚠️ Topic {} has only {} {} questions, but {} requested. Using fallback.", 
            topic.getName(), questions.size(), difficulty, count);
        
        List<Question> result = new ArrayList<>(questions); // Take all available
        int remaining = count - result.size();
        
        log.debug("Starting fallback: have {}, need {} more", result.size(), remaining);
        
        // Try other difficulty levels
        List<DifficultyLevel> otherLevels = Arrays.stream(DifficultyLevel.values())
            .filter(level -> level != difficulty)
            .collect(Collectors.toList());
        Collections.shuffle(otherLevels);
        
        for (DifficultyLevel fallbackLevel : otherLevels) {
            if (remaining <= 0) break;
            
            log.debug("Trying fallback level: {}", fallbackLevel);
            
            List<Question> fallbackQuestions = questionRepository
                .findByTopic_TopicIdAndDifficultyLevelAndIsQuestionBankTrue(
                    topic.getTopicId(), fallbackLevel);
            
            log.debug("Found {} {} questions for fallback", fallbackQuestions.size(), fallbackLevel);
            
            // Remove already selected questions
            fallbackQuestions.removeIf(q -> result.contains(q));
            
            if (!fallbackQuestions.isEmpty()) {
                Collections.shuffle(fallbackQuestions);
                int toTake = Math.min(remaining, fallbackQuestions.size());
                result.addAll(fallbackQuestions.subList(0, toTake));
                remaining -= toTake;
                log.info("✅ Added {} {} questions from fallback for topic {} (remaining: {})", 
                    toTake, fallbackLevel, topic.getName(), remaining);
            } else {
                log.debug("No {} questions available for fallback", fallbackLevel);
            }
        }
        
        // If still not enough, throw exception with detailed info
        if (result.size() < count) {
            log.error("❌ INSUFFICIENT QUESTIONS for topic {} (ID={}): Requested={}, Available={}, Difficulty={}", 
                topic.getName(), topic.getTopicId(), count, result.size(), difficulty);
            
            throw new InsufficientQuestionsException(
                topic.getTopicId(),
                topic.getName(),
                count,
                result.size(),
                difficulty.name()
            );
        }
        
        log.info("✅ Fallback successful: Collected {} questions for topic {}", result.size(), topic.getName());
        return result;
    }
    
    /**
     * Map string difficulty to enum
     */
    private DifficultyLevel mapDifficultyLevel(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> DifficultyLevel.EASY;
            case "medium" -> DifficultyLevel.MEDIUM;
            case "hard" -> DifficultyLevel.HARD;
            default -> throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
        };
    }
    
    /**
     * Build complete assessment response DTO
     */
    private AssessmentResponseDTO buildAssessmentResponse(
            List<Question> questions, 
            Subject subject,
            String difficulty,
            Map<String, Integer> topicDistribution,
            Map<String, Integer> difficultyDistribution) {
        
        // Generate unique assessment ID
        String assessmentId = UUID.randomUUID().toString();
        
        // Convert questions to DTOs (without isCorrect flag for security)
        List<AssessmentQuestionDTO> questionDTOs = questions.stream()
            .map(this::convertToAssessmentQuestionDTO)
            .collect(Collectors.toList());
        
        // Build distribution DTO
        AssessmentDistributionDTO distribution = new AssessmentDistributionDTO();
        distribution.setByTopic(topicDistribution);
        distribution.setByDifficulty(difficultyDistribution);
        
        // Build response
        AssessmentResponseDTO response = new AssessmentResponseDTO();
        response.setAssessmentId(assessmentId);
        response.setSubjectId(subject.getSubjectId());
        response.setSubjectName(subject.getName());
        response.setTotalQuestions(questions.size());
        response.setDifficulty(difficulty);
        response.setQuestions(questionDTOs);
        response.setDistribution(distribution);
        response.setCreatedAt(LocalDateTime.now());
        
        return response;
    }
    
    /**
     * Convert Question entity to AssessmentQuestionDTO
     * NOTE: Options are shuffled and isCorrect flag is INCLUDED
     * (Frontend will remove it before displaying to student)
     */
    private AssessmentQuestionDTO convertToAssessmentQuestionDTO(Question question) {
        AssessmentQuestionDTO dto = new AssessmentQuestionDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setQuestionText(question.getQuestionText());
        dto.setDifficultyLevel(question.getDifficultyLevel().name());
        
        if (question.getTopic() != null) {
            dto.setTopicId(question.getTopic().getTopicId());
            dto.setTopicName(question.getTopic().getName());
        }
        
        // Convert and shuffle options
        List<AssessmentQuestionDTO.AssessmentOptionDTO> optionDTOs = question.getOptions().stream()
            .map(this::convertToAssessmentOptionDTO)
            .collect(Collectors.toList());
        Collections.shuffle(optionDTOs);
        dto.setOptions(optionDTOs);
        
        dto.setExplanation(null); // Will be shown after submission
        
        return dto;
    }
    
    /**
     * Convert QuestionOption to DTO
     */
    private AssessmentQuestionDTO.AssessmentOptionDTO convertToAssessmentOptionDTO(QuestionOption option) {
        AssessmentQuestionDTO.AssessmentOptionDTO dto = new AssessmentQuestionDTO.AssessmentOptionDTO();
        dto.setOptionId(option.getOptionId());
        dto.setOptionText(option.getOptionText());
        dto.setIsCorrect(option.getIsCorrect()); // Included for backend processing
        return dto;
    }
}
