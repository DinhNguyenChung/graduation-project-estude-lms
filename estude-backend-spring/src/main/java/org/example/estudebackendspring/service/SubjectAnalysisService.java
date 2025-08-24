package org.example.estudebackendspring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private static final String AI_URL = "http://127.0.0.1:8000/predict/subjects";

    @Transactional
    public JsonNode analyzeSubjectsAndSave(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<SubjectGrade> grades = subjectGradeRepository.findByStudentIdWithSubject(studentId);

        ObjectNode payload = objectMapper.createObjectNode();
        // map subject grades -> payload
        for (SubjectGrade sg : grades) {
            ClassSubject cs = sg.getClassSubject();
            String subjectName = cs != null && cs.getSubject() != null ? cs.getSubject().getName() : "UNKNOWN";

            ObjectNode subjectNode = objectMapper.createObjectNode();

            int idx = 1;
            if (sg.getRegularScores() != null) {
                for (Float score : sg.getRegularScores()) {
                    if (score != null) subjectNode.put("diem_" + idx, score);
                    idx++;
                }
            }
            if (sg.getMidtermScore() != null) subjectNode.put("diem_gk", sg.getMidtermScore());
            if (sg.getFinalScore() != null) subjectNode.put("diem_ck", sg.getFinalScore());

            payload.set(subjectName, subjectNode);
        }

        // Lưu request
        AIAnalysisRequest req = new AIAnalysisRequest();
        req.setRequestDate(LocalDateTime.now());
        req.setAnalysisType(AnalysisType.PREDICT_SUBJECT_GRADE);
        req.setStudent(student);
        req.setDataPayload(payload);
        req = requestRepository.save(req);
        System.out.println("payload: " + payload);
        // Gọi AI
        JsonNode aiResponse;
        try {
            aiResponse = restTemplate.postForObject(AI_URL, payload, JsonNode.class);
            System.out.println("payload: " + payload);
        } catch (RestClientException ex) {
            req.setDataPayload(payload);
            requestRepository.save(req);
            throw new RuntimeException("Call to AI service failed: " + ex.getMessage(), ex);
        }

        // Lưu result (tạm thời)
        AIAnalysisResult result = new AIAnalysisResult();
        result.setDetailedAnalysis(aiResponse);
        if (aiResponse != null && aiResponse.has("suggested_actions"))
            result.setSuggestedActions(aiResponse.get("suggested_actions"));
        if (aiResponse != null && aiResponse.has("statistics"))
            result.setStatistics(aiResponse.get("statistics"));
        result.setGeneratedAt(LocalDateTime.now());
        result.setRequestId(req.getRequestId());
        result = resultRepository.save(result);

        // --- MỚI: nếu AI trả dự đoán cho từng môn thì cập nhật SubjectGrade tương ứng ---
        if (aiResponse != null && aiResponse.isObject()) {
            // Duyệt các môn trong aiResponse
            aiResponse.fieldNames().forEachRemaining(subjectKey -> {
                JsonNode subjNode = aiResponse.get(subjectKey);
                if (subjNode == null || !subjNode.isObject()) return;

                // tìm SubjectGrade tương ứng trong grades list theo tên môn
                SubjectGrade matched = grades.stream()
                        .filter(sg -> {
                            ClassSubject cs = sg.getClassSubject();
                            String name = cs != null && cs.getSubject() != null ? cs.getSubject().getName() : null;
                            return name != null && name.equals(subjectKey);
                        })
                        .findFirst()
                        .orElse(null);

                if (matched == null) {
                    log.warn("Subject not found: {}", subjectKey);
                    return;
                }

                try {
                    Float predictedGk = findFirstFloat(subjNode,
                            "diem_gk_du_doan", "diem_gk_thuc_te", "diem_gk", "diem_gk_du_doan");
                    if (predictedGk != null) {
                        matched.setPredictedMidTerm(predictedGk);
                    }

                    Float predictedCk = findFirstFloat(subjNode,
                            "diem_ck_du_doan", "diem_ck", "diem_cuoi_ki_du_doan");
                    if (predictedCk != null) {
                        matched.setPredictedFinal(predictedCk);
                    }

                    Float predictedAvg = findFirstFloat(subjNode,
                            "diem_tb_du_doan", "diem_tb_du_doan", "diem_tb_du_doan");
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

            // Lưu tất cả SubjectGrade thay đổi (explicit save để chắc chắn)
            subjectGradeRepository.saveAll(grades);
        }

        // cập nhật request->result liên kết
        req.setResult(result);
        requestRepository.save(req);

        // Nếu AI trả một trường global predictedAverage hoặc tương tự, ghi vào AIAnalysisResult.predictedAverage
        Float overallPredicted = null;
        if (aiResponse != null) {
            overallPredicted = findFirstFloat(aiResponse, "predicted_average", "overall_predicted_average", "diem_tb_du_doan");
        }
        if (overallPredicted != null) {
            result.setPredictedAverage(overallPredicted);
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
