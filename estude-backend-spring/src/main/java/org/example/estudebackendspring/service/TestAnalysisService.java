package org.example.estudebackendspring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.AIAnalysisRequest;
import org.example.estudebackendspring.entity.AIAnalysisResult;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.enums.AnalysisType;
import org.example.estudebackendspring.repository.AIAnalysisRequestRepository;
import org.example.estudebackendspring.repository.AIAnalysisResultRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.repository.SubmissionReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AIAnalysisService.class);

    private final SubmissionReportRepository submissionReportRepository;
    private final StudentRepository studentRepository;
    private final AIAnalysisRequestRepository requestRepository;
    private final AIAnalysisResultRepository resultRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // AI endpoint
    private static final String AI_URL = "http://127.0.0.1:8000/test/analyze";

    /**
     * Lấy bài làm (JSON) -> gửi AI -> lưu request & result -> trả về JsonNode response.
     */
    @Transactional
    public JsonNode analyzeStudentSubmission(Long assignmentId, Long studentId) {
        // 1. lấy payload JSON (string) từ DB
        Optional<String> payloadOpt = submissionReportRepository.getStudentSubmissionJson(assignmentId, studentId);
        if (payloadOpt.isEmpty()) {
            // không có bài làm
            return objectMapper.createObjectNode().put("message", "Không tìm thấy bài làm cho học sinh này");
        }

        String payloadText = payloadOpt.get();
        if (payloadText == null || payloadText.isBlank()) {
            return objectMapper.createObjectNode().put("message", "Payload trống");
        }

        JsonNode payloadNode;
        try {
            payloadNode = objectMapper.readTree(payloadText);
        } catch (Exception ex) {
            log.error("Error parsing payload JSON from DB", ex);
            throw new RuntimeException("Invalid payload JSON", ex);
        }

        // 2. tìm student để gắn vào request (nếu cần)
        Student student = studentRepository.findById(studentId)
                .orElse(null); // không bắt buộc, nhưng gắn nếu có

        // 3. lưu AIAnalysisRequest
        AIAnalysisRequest req = new AIAnalysisRequest();
        req.setRequestDate(LocalDateTime.now());
        req.setAnalysisType(AnalysisType.ANALYZE_TEST); // hoặc enum phù hợp
        req.setStudent(student);
        req.setDataPayload(payloadNode);
        req = requestRepository.save(req); // lưu để có requestId
        // Chuẩn hoá payload: đảm bảo assignment_id là string
        if (!payloadNode.isObject()) {
            throw new RuntimeException("Payload is not a JSON object");
        }
        ObjectNode payloadObj = (ObjectNode) payloadNode;
        if (payloadObj.has("assignment_id") && payloadObj.get("assignment_id").isNumber()) {
            // convert number -> string (ví dụ 3 -> "3")
            payloadObj.put("assignment_id", payloadObj.get("assignment_id").asText());
        }
        // 4. gọi AI service (POST JSON)
        JsonNode aiResponse;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JsonNode> entity = new HttpEntity<>(payloadNode, headers);

            ResponseEntity<JsonNode> resp = restTemplate.postForEntity(AI_URL, entity, JsonNode.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                aiResponse = resp.getBody();
            } else {
                log.error("AI service returned non-2xx or empty body: {}", resp.getStatusCode());
                throw new RuntimeException("AI service error: " + resp.getStatusCode());
            }
        } catch (Exception ex) {
            log.error("Call to AI service failed", ex);
            // bạn có thể update req.comment hoặc saved flag nếu muốn
            throw new RuntimeException("Call to AI service failed: " + ex.getMessage(), ex);
        }

        // 5. lưu AIAnalysisResult (map dữ liệu nếu có)
        AIAnalysisResult result = new AIAnalysisResult();
        // store detailedAnalysis as the AI's 'data' if exists else whole response
        JsonNode dataNode = aiResponse.has("data") ? aiResponse.get("data") : aiResponse;
        result.setDetailedAnalysis(dataNode);

        // suggestedActions / statistics if present
        if (aiResponse.has("suggested_actions")) result.setSuggestedActions(aiResponse.get("suggested_actions"));
        if (aiResponse.has("statistics")) result.setStatistics(aiResponse.get("statistics"));

        // map some common fields from AI response data if present
        if (dataNode != null) {
            // example: accuracy_percentage -> predictedAverage (as float)
            if (dataNode.has("accuracy_percentage") && dataNode.get("accuracy_percentage").isNumber()) {
                result.setPredictedAverage((float) dataNode.get("accuracy_percentage").asDouble());
            }
            // performance level
            if (dataNode.has("performance_level")) {
                result.setPredictedPerformance(dataNode.get("performance_level").asText());
            }
            // general recommendation -> comment
            if (dataNode.has("general_recommendation")) {
                result.setComment(dataNode.get("general_recommendation").asText());
            }
        }

        result.setGeneratedAt(LocalDateTime.now());
        result.setRequestId(req.getRequestId());
        result = resultRepository.save(result);

        // 6. liên kết request->result và lưu lại
        req.setResult(result);
        requestRepository.save(req);

        // 7. trả về response AI cho client
        return aiResponse;
    }
}