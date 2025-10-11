package org.example.estudebackendspring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AnalysisType;
import org.example.estudebackendspring.repository.AIAnalysisRequestRepository;
import org.example.estudebackendspring.repository.AIAnalysisResultRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.repository.SubjectGradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(SubjectAnalysisService.class);

    private final SubjectGradeRepository subjectGradeRepository;
    private final StudentRepository studentRepository;
    private final AIAnalysisRequestRepository requestRepository;
    private final AIAnalysisResultRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url}")
    private String aiServiceUrl;
    private String AI_URL ;
    @PostConstruct
    public void init() {
        AI_URL = aiServiceUrl+ "/predict/subjects";
    }

    @Transactional
    public JsonNode analyzeSubjectsAndSave(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<SubjectGrade> grades = subjectGradeRepository.findByStudentIdWithSubject(studentId);

        // Tạo payload với cấu trúc {"subjects": {...}}
        ObjectNode payload = objectMapper.createObjectNode();
        ObjectNode subjectsNode = objectMapper.createObjectNode();

        // Map subject grades -> payload
        for (SubjectGrade sg : grades) {
            ClassSubject cs = sg.getClassSubject();
            String subjectName = cs != null && cs.getSubject() != null ? cs.getSubject().getName() : "UNKNOWN";

            ObjectNode subjectNode = objectMapper.createObjectNode();

            int idx = 1;
            if (sg.getRegularScores() != null) {
                for (Float score : sg.getRegularScores()) {
                    if (score != null) {
                        // Sử dụng "tx" thay vì "diem_" để khớp với input AI
                        subjectNode.put("tx" + idx, score);
                    }
                    idx++;
                }
            }
            if (sg.getMidtermScore() != null) {
                subjectNode.put("gk", sg.getMidtermScore()); // Sử dụng "gk" thay vì "diem_gk"
            }
            if (sg.getFinalScore() != null) {
                subjectNode.put("diem_ck", sg.getFinalScore());
            }

            subjectsNode.set(subjectName, subjectNode);
        }
        payload.set("subjects", subjectsNode);

        // Lưu request
        AIAnalysisRequest req = new AIAnalysisRequest();
        req.setRequestDate(LocalDateTime.now());
        req.setAnalysisType(AnalysisType.PREDICT_SUBJECT_GRADE);
        req.setStudent(student);
        req.setDataPayload(payload);
        req = requestRepository.save(req);
        log.info("Payload sent to AI: {}", payload);

        // Gọi AI
        JsonNode aiResponse;
        try {
            aiResponse = restTemplate.postForObject(AI_URL, payload, JsonNode.class);
            log.info("AI response: {}", aiResponse);
        } catch (RestClientException ex) {
            req.setDataPayload(payload);
            requestRepository.save(req);
            throw new RuntimeException("Call to AI service failed: " + ex.getMessage(), ex);
        }

        // Xử lý lỗi từ AI
        if (aiResponse != null && aiResponse.has("success") && !aiResponse.get("success").asBoolean()) {
            String errorMsg = aiResponse.has("error") ? aiResponse.get("error").asText() : "Unknown AI service error";
            log.error("AI service returned error: {}. Payload: {}", errorMsg, payload.toString());
            throw new RuntimeException("AI service error: " + errorMsg);
        }

        // Lưu result
        AIAnalysisResult result = new AIAnalysisResult();
        result.setDetailedAnalysis(aiResponse);
        if (aiResponse != null && aiResponse.has("data")) {
            JsonNode dataNode = aiResponse.get("data");
            if (dataNode.has("suggested_actions")) {
                result.setSuggestedActions(dataNode.get("suggested_actions"));
            }
            if (dataNode.has("statistics")) {
                result.setStatistics(dataNode.get("statistics"));
            }
        }
        result.setGeneratedAt(LocalDateTime.now());
        result.setRequestId(req.getRequestId());
        result = resultRepository.save(result);

        // Cập nhật SubjectGrade từ AI response
        if (aiResponse != null && aiResponse.has("data") && aiResponse.get("data").has("predictions")) {
            JsonNode predictions = aiResponse.get("data").get("predictions");

            predictions.fieldNames().forEachRemaining(subjectKey -> {
                JsonNode subjNode = predictions.get(subjectKey);
                if (subjNode == null || !subjNode.isObject()) {
                    log.warn("Invalid data for subject: {}. Skipping.", subjectKey);
                    return;
                }

                // Tìm SubjectGrade tương ứng
                SubjectGrade matched = grades.stream()
                        .filter(sg -> {
                            ClassSubject cs = sg.getClassSubject();
                            String name = cs != null && cs.getSubject() != null ? cs.getSubject().getName() : null;
                            return name != null && name.equals(subjectKey);
                        })
                        .findFirst()
                        .orElse(null);

                if (matched == null) {
                    log.warn("Subject not found in database: {}. Payload: {}", subjectKey, payload.toString());
                    return;
                }

                try {
                    Float predictedGk = findFirstFloat(subjNode,
                            "diem_gk_du_doan", "diem_gk_thuc_te", "diem_gk");
                    if (predictedGk != null) {
                        matched.setPredictedMidTerm(predictedGk);
                    }

                    Float predictedCk = findFirstFloat(subjNode,
                            "diem_ck_du_doan", "diem_ck");
                    if (predictedCk != null) {
                        matched.setPredictedFinal(predictedCk);
                    }

                    Float predictedAvg = findFirstFloat(subjNode,
                            "diem_tb_du_doan");
                    if (predictedAvg != null) {
                        matched.setPredictedAverage(predictedAvg);
                    }

                    if (subjNode.has("nhan_xet") && subjNode.get("nhan_xet").isTextual()) {
                        matched.setComment(subjNode.get("nhan_xet").asText());
                    }

                } catch (Exception ex) {
                    log.warn("Error parsing AI response for subject {}: {}", subjectKey, ex.getMessage());
                }
            });

            // Lưu tất cả SubjectGrade thay đổi
            subjectGradeRepository.saveAll(grades);
        }

        // Cập nhật liên kết request->result
        req.setResult(result);
        requestRepository.save(req);

        // Xử lý overall predicted average
        Float overallPredicted = null;
        if (aiResponse != null && aiResponse.has("data")) {
            JsonNode dataNode = aiResponse.get("data");
            overallPredicted = findFirstFloat(dataNode, "predicted_average", "overall_predicted_average", "diem_tb_du_doan");
        }
        if (overallPredicted != null) {
            result.setPredictedAverage(overallPredicted);
            resultRepository.save(result);
        }

        // Lưu processed_subjects và total_subjects vào additionalInfo nếu có
        if (aiResponse != null && aiResponse.has("data")) {
            JsonNode dataNode = aiResponse.get("data");
            ObjectNode additionalInfo = objectMapper.createObjectNode();
            if (dataNode.has("processed_subjects")) {
                additionalInfo.set("processed_subjects", dataNode.get("processed_subjects"));
            }
            if (dataNode.has("total_subjects")) {
                additionalInfo.put("total_subjects", dataNode.get("total_subjects").asInt());
            }
//            result.setAdditionalInfo(additionalInfo);
            resultRepository.save(result);
        }

        return aiResponse != null ? aiResponse : objectMapper.createObjectNode();
    }

    private Float findFirstFloat(JsonNode node, String... candidateKeys) {
        for (String k : candidateKeys) {
            if (node.has(k)) {
                JsonNode v = node.get(k);
                if (v == null || v.isNull()) continue;
                if (v.isNumber()) {
                    return (float) v.asDouble();
                }
                if (v.isTextual()) {
                    try {
                        return Float.valueOf(v.asText());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return null;
    }
}
