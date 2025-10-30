package org.example.estudebackendspring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.learning.*;
import org.example.estudebackendspring.entity.AIAnalysisRequest;
import org.example.estudebackendspring.entity.AIAnalysisResult;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.enums.AnalysisType;
import org.example.estudebackendspring.repository.AIAnalysisRequestRepository;
import org.example.estudebackendspring.repository.AIAnalysisResultRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@Slf4j
public class LearningLoopService {
    
    private final StudentRepository studentRepository;
    private final AIAnalysisRequestRepository requestRepository;
    private final AIAnalysisResultRepository resultRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public LearningLoopService(StudentRepository studentRepository,
                               AIAnalysisRequestRepository requestRepository,
                               AIAnalysisResultRepository resultRepository) {
        this.studentRepository = studentRepository;
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;

        //Khởi tạo ObjectMapper và đăng ký module xử lý LocalDateTime
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Value("${ai.service.url}")
    private String aiServiceUrl;
    
    private String FEEDBACK_URL;
    private String RECOMMENDATION_URL;
    private String PRACTICE_QUIZ_URL;
    private String PRACTICE_REVIEW_URL;  // Layer 3.5
    private String IMPROVEMENT_URL;
    private String FULL_LOOP_URL;
    
    @PostConstruct
    public void init() {
        FEEDBACK_URL = aiServiceUrl + "/api/ai/learning-feedback";
        RECOMMENDATION_URL = aiServiceUrl + "/api/ai/learning-recommendation";
        PRACTICE_QUIZ_URL = aiServiceUrl + "/api/ai/generate-practice-quiz";
        PRACTICE_REVIEW_URL = aiServiceUrl + "/api/ai/review-practice-results";  // Layer 3.5
        IMPROVEMENT_URL = aiServiceUrl + "/api/ai/improvement-evaluation";
        FULL_LOOP_URL = aiServiceUrl + "/api/ai/full-learning-loop";
    }
//        RECOMMENDATION_URL = aiServiceUrl + "/api/ai/learning-recommendation";
//        PRACTICE_QUIZ_URL = aiServiceUrl + "/api/ai/generate-practice-quiz";
//        IMPROVEMENT_URL = aiServiceUrl + "/api/ai/improvement-evaluation";
//        FULL_LOOP_URL = aiServiceUrl + "/api/ai/full-learning-loop";
//    }
    
    /**
     * Layer 1: Learning Feedback - Phân tích chi tiết từng câu hỏi
     */
    @Transactional
    public FeedbackResponse getLearningFeedback(FeedbackRequest request) {
        log.info("Getting learning feedback for assignment: {}, student: {}", 
                request.getAssessmentId(), request.getStudentName());
        
        // Lưu request
        AIAnalysisRequest analysisRequest = saveAnalysisRequest(
            request.getAssessmentId(),
            AnalysisType.LEARNING_FEEDBACK,
            objectMapper.valueToTree(request)
        );
        
        try {
            // Gọi AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FeedbackRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<FeedbackResponse> response = restTemplate.postForEntity(
                FEEDBACK_URL, entity, FeedbackResponse.class
            );
            
            FeedbackResponse feedbackResponse = response.getBody();
            
            if (feedbackResponse != null && feedbackResponse.getSuccess()) {
                // Lưu result và lấy result_id
                AIAnalysisResult savedResult = saveAnalysisResult(
                    analysisRequest.getRequestId(),
                    objectMapper.valueToTree(feedbackResponse.getData()),
                    "Learning feedback completed successfully"
                );
                
                // Gắn result_id vào response để FE có thể dùng cho Layer 4
                if (feedbackResponse.getData() != null) {
                    feedbackResponse.getData().setResultId(savedResult.getResultId());
                    log.info("✅ Layer 1 result_id set: {}", savedResult.getResultId());
                }
            }
            
            return feedbackResponse;
            
        } catch (Exception ex) {
            log.error("Error getting learning feedback", ex);
            saveAnalysisResult(
                analysisRequest.getRequestId(),
                null,
                "Error: " + ex.getMessage()
            );
            throw new RuntimeException("Failed to get learning feedback: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Layer 2: Learning Recommendation - Đưa ra gợi ý học tập
     */
    @Transactional
    public RecommendationResponse getLearningRecommendation(RecommendationRequest request) {
        log.info("Getting learning recommendation for student: {}", 
                request.getFeedbackData().getStudentName());
        
        // Lưu request
        AIAnalysisRequest analysisRequest = saveAnalysisRequest(
            null,
            AnalysisType.LEARNING_RECOMMENDATION,
            objectMapper.valueToTree(request)
        );
        
        try {
            // Gọi AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecommendationRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<RecommendationResponse> response = restTemplate.postForEntity(
                RECOMMENDATION_URL, entity, RecommendationResponse.class
            );
            
            RecommendationResponse recommendationResponse = response.getBody();
            
            if (recommendationResponse != null && recommendationResponse.getSuccess()) {
                // Lưu result
                saveAnalysisResult(
                    analysisRequest.getRequestId(),
                    objectMapper.valueToTree(recommendationResponse.getData()),
                    "Learning recommendation completed successfully"
                );
            }
            
            return recommendationResponse;
            
        } catch (Exception ex) {
            log.error("Error getting learning recommendation", ex);
            saveAnalysisResult(
                analysisRequest.getRequestId(),
                null,
                "Error: " + ex.getMessage()
            );
            throw new RuntimeException("Failed to get learning recommendation: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Layer 3: Practice Quiz Generation - Sinh bộ câu hỏi luyện tập
     */
    @Transactional
    public PracticeQuizResponse generatePracticeQuiz(PracticeQuizRequest request) {
        log.info("Generating practice quiz for subject: {}, topics: {}", 
                request.getSubject(), request.getTopics());
        
        // Lưu request
        AIAnalysisRequest analysisRequest = saveAnalysisRequest(
            null,
            AnalysisType.PRACTICE_QUIZ,
            objectMapper.valueToTree(request)
        );
        
        try {
            // Gọi AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PracticeQuizRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PracticeQuizResponse> response = restTemplate.postForEntity(
                PRACTICE_QUIZ_URL, entity, PracticeQuizResponse.class
            );
            
            PracticeQuizResponse quizResponse = response.getBody();
            
            if (quizResponse != null && quizResponse.getSuccess()) {
                // Lưu result
                saveAnalysisResult(
                    analysisRequest.getRequestId(),
                    objectMapper.valueToTree(quizResponse.getData()),
                    "Practice quiz generated successfully"
                );
            }
            
            return quizResponse;
            
        } catch (Exception ex) {
            log.error("Error generating practice quiz", ex);
            saveAnalysisResult(
                analysisRequest.getRequestId(),
                null,
                "Error: " + ex.getMessage()
            );
            throw new RuntimeException("Failed to generate practice quiz: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Layer 3.5: Review Practice Results - Xem lại kết quả bài luyện tập
     */
    @Transactional
    public ReviewPracticeResponse reviewPracticeResults(ReviewPracticeRequest request) {
        log.info("Reviewing practice results for student: {}, subject: {}", 
                request.getStudentName(), request.getSubject());
        
        // Lưu request
        AIAnalysisRequest analysisRequest = saveAnalysisRequest(
            null,
            AnalysisType.PRACTICE_REVIEW,
            objectMapper.valueToTree(request)
        );
        
        try {
            // Gọi AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ReviewPracticeRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ReviewPracticeResponse> response = restTemplate.postForEntity(
                PRACTICE_REVIEW_URL, entity, ReviewPracticeResponse.class
            );
            
            ReviewPracticeResponse reviewResponse = response.getBody();
            
            if (reviewResponse != null && reviewResponse.getSuccess()) {
                // Lưu result và lấy result_id
                AIAnalysisResult savedResult = saveAnalysisResult(
                    analysisRequest.getRequestId(),
                    objectMapper.valueToTree(reviewResponse.getData()),
                    "Practice review completed successfully"
                );
                
                // Gắn result_id vào response để FE có thể dùng cho Layer 4
                if (reviewResponse.getData() != null) {
                    reviewResponse.getData().setResultId(savedResult.getResultId());
                    log.info("✅ Layer 3.5 result_id set: {}", savedResult.getResultId());
                }
            }
            
            return reviewResponse;
            
        } catch (Exception ex) {
            log.error("Error reviewing practice results", ex);
            saveAnalysisResult(
                analysisRequest.getRequestId(),
                null,
                "Error: " + ex.getMessage()
            );
            throw new RuntimeException("Failed to review practice results: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Layer 4: Improvement Evaluation - Đánh giá tiến bộ
     */
    @Transactional
    public ImprovementResponse evaluateImprovement(ImprovementRequest request) {
        log.info("Evaluating improvement for student: {}, subject: {}",
                request.getStudentId(), request.getSubject());
        
        // Lưu request
        AIAnalysisRequest analysisRequest = saveAnalysisRequest(
            request.getStudentId().toString(),
            AnalysisType.IMPROVEMENT_EVALUATION,
            objectMapper.valueToTree(request)
        );
        
        try {
            // Gọi AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ImprovementRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ImprovementResponse> response = restTemplate.postForEntity(
                IMPROVEMENT_URL, entity, ImprovementResponse.class
            );
            
            ImprovementResponse improvementResponse = response.getBody();
            
            if (improvementResponse != null && improvementResponse.getSuccess()) {
                // Lưu result
                saveAnalysisResult(
                    analysisRequest.getRequestId(),
                    objectMapper.valueToTree(improvementResponse.getData()),
                    "Improvement evaluation completed successfully"
                );
            }
            
            return improvementResponse;
            
        } catch (Exception ex) {
            log.error("Error evaluating improvement", ex);
            saveAnalysisResult(
                analysisRequest.getRequestId(),
                null,
                "Error: " + ex.getMessage()
            );
            throw new RuntimeException("Failed to evaluate improvement: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Full Learning Loop - Chạy toàn bộ Layer 1, 2, 3 cùng lúc
     */
    @Transactional
    public FullLearningLoopResponse runFullLearningLoop(FeedbackRequest request) {
        log.info("Running full learning loop for assignment: {}, student: {}", 
                request.getAssessmentId(), request.getStudentName());
        
        // Lưu request
        AIAnalysisRequest analysisRequest = saveAnalysisRequest(
            request.getAssessmentId(),
            AnalysisType.FULL_LEARNING_LOOP,
            objectMapper.valueToTree(request)
        );
        
        try {
            // Gọi AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FeedbackRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<FullLearningLoopResponse> response = restTemplate.postForEntity(
                FULL_LOOP_URL, entity, FullLearningLoopResponse.class
            );
            
            FullLearningLoopResponse loopResponse = response.getBody();
            
            if (loopResponse != null && loopResponse.getSuccess()) {
                // Lưu result
                saveAnalysisResult(
                    analysisRequest.getRequestId(),
                    objectMapper.valueToTree(loopResponse.getData()),
                    "Full learning loop completed successfully"
                );
            }
            
            return loopResponse;
            
        } catch (Exception ex) {
            log.error("Error running full learning loop", ex);
            saveAnalysisResult(
                analysisRequest.getRequestId(),
                null,
                "Error: " + ex.getMessage()
            );
            throw new RuntimeException("Failed to run full learning loop: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Helper method: Lưu analysis request
     * Tự động gắn student từ SecurityContext (người dùng đang đăng nhập)
     */
    private AIAnalysisRequest saveAnalysisRequest(String identifier, AnalysisType analysisType, Object payload) {
        AIAnalysisRequest request = new AIAnalysisRequest();
        request.setRequestDate(LocalDateTime.now());
        request.setAnalysisType(analysisType);
        request.setDataPayload(objectMapper.valueToTree(payload));
        
        // Luôn ưu tiên lấy student từ SecurityContext (người dùng đang đăng nhập)
        var auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🔍 [{}] SecurityContext authentication: {}", analysisType, auth != null ? auth.getName() : "NULL");
        
        if (auth != null && auth.getPrincipal() instanceof org.example.estudebackendspring.entity.User user) {
            Long uid = user.getUserId();
            log.info("✅ [{}] Found User from SecurityContext - userId: {}", analysisType, uid);
            if (uid != null) {
                studentRepository.findById(uid).ifPresentOrElse(
                    student -> {
                        request.setStudent(student);
                        log.info("✅ [{}] Student linked successfully - studentId: {}", analysisType, uid);
                    },
                    () -> log.warn("⚠️ [{}] Student with userId {} not found in database", analysisType, uid)
                );
            } else {
                log.warn("⚠️ [{}] User.userId is NULL", analysisType);
            }
        } else {
            log.warn("⚠️ [{}] No User found in SecurityContext or Principal is not User type", analysisType);
        }
        
        // Nếu identifier được cung cấp và là số, có thể override (dành cho admin/teacher)
        // Bình thường sẽ không cần vì đã lấy từ SecurityContext
        if (identifier != null && request.getStudent() == null) {
            try {
                Long studentId = Long.parseLong(identifier);
                log.info("🔄 [{}] Trying to override with identifier: {}", analysisType, studentId);
                studentRepository.findById(studentId).ifPresentOrElse(
                    student -> {
                        request.setStudent(student);
                        log.info("✅ [{}] Student linked via identifier - studentId: {}", analysisType, studentId);
                    },
                    () -> log.warn("⚠️ [{}] Student with identifier {} not found", analysisType, studentId)
                );
            } catch (NumberFormatException e) {
                log.debug("ℹ️ [{}] Identifier '{}' is not a number, skipping", analysisType, identifier);
            }
        }
        
        AIAnalysisRequest savedRequest = requestRepository.save(request);
        log.info("💾 [{}] AIAnalysisRequest saved - requestId: {}, studentId: {}", 
                analysisType, savedRequest.getRequestId(), 
                savedRequest.getStudent() != null ? savedRequest.getStudent().getUserId() : "NULL");
        
        return savedRequest;
    }
    
    /**
     * Helper method: Lưu analysis result
     */
    private AIAnalysisResult saveAnalysisResult(Long requestId, Object data, String comment) {
        AIAnalysisResult result = new AIAnalysisResult();
        result.setRequestId(requestId);
        result.setGeneratedAt(LocalDateTime.now());
        result.setComment(comment);
        
        if (data != null) {
            result.setDetailedAnalysis(objectMapper.valueToTree(data));
        }
        
        return resultRepository.save(result);
    }
}
