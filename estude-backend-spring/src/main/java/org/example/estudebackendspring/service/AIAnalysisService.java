package org.example.estudebackendspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.example.estudebackendspring.dto.AiPredictPayload;
import org.example.estudebackendspring.dto.AiPredictResponse;
import org.example.estudebackendspring.dto.AiPredictResponseWrapper;
import org.example.estudebackendspring.entity.AIAnalysisRequest;
import org.example.estudebackendspring.entity.AIAnalysisResult;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.enums.AnalysisType;
import org.example.estudebackendspring.repository.AIAnalysisRequestRepository;
import org.example.estudebackendspring.repository.AIAnalysisResultRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AIAnalysisService  {
    private final StudentRepository studentRepository;
    private final AIAnalysisRequestRepository requestRepository;
    private final AIAnalysisResultRepository resultRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://127.0.0.1:8000/predict}")
    private String aiServiceUrl;

    public AIAnalysisService(StudentRepository studentRepository,
                             AIAnalysisRequestRepository requestRepository,
                             AIAnalysisResultRepository resultRepository) {
        this.studentRepository = studentRepository;
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Thực hiện luồng: lấy điểm từ DB -> gọi AI service -> lưu request/result -> trả về AIAnalysisResult
     */
    @Transactional
    public AIAnalysisResult analyzePredict(Long studentUserId) {
        // 1) Lấy student
        Student student = studentRepository.findById(studentUserId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy ID sinh viên: " + studentUserId));

        // 2) Lấy điểm thô từ repository (nativeQuery trả List<Object[]>)
        List<Object[]> rows = studentRepository.findGradesByStudentId(studentUserId);

        // 3) Kiểm tra predicted_average presence và build gradesMap chỉ từ predicted_average
        Map<String, Float> gradesMap = new HashMap<>();
        List<String> missingPredictedSubjects = new ArrayList<>();

        if (rows == null || rows.isEmpty()) {
            missingPredictedSubjects.add("Không tìm thấy bất kỳ điểm nào cho học sinh này.");
        } else {
            for (Object[] r : rows) {
                String subjectName = r[3] != null ? r[3].toString() : "UNKNOWN";

                // predicted_average tại vị trí 9 theo SELECT của bạn
//                Double predicted = toDouble(r[9]);
                Float predicted = Float.parseFloat(r[9]!=null ? r[9].toString() : null);
//                System.out.println("Raw predicted_average for " + subjectName + ": " + r[9] + ", type: " + (r[9] != null ? r[9].getClass().getName() : "null") + ", converted: " + predicted);
                if (predicted == null) {
                    missingPredictedSubjects.add(subjectName);
                } else {
                    gradesMap.put(subjectName, predicted);
                }
            }
        }

        // 4) Build và lưu AIAnalysisRequest (luôn lưu request để trace)
        AIAnalysisRequest req = new AIAnalysisRequest();
        req.setRequestDate(LocalDateTime.now());
        req.setAnalysisType(AnalysisType.PREDICT_SEMESTER_PERFORMANCE);

        // payload: include studentId and gradesMap (even nếu thiếu predicted, để trace)
        String studentIdStr = student.getUserId().toString();
        if (studentIdStr == null || studentIdStr.trim().isEmpty() ) {
            throw new IllegalArgumentException("Id sinh viên không thể để trống hoặc null để dự đoán AI");
        }

        AiPredictPayload payload = new AiPredictPayload(
                studentIdStr,
                gradesMap,
                95.0,
                2.0,
                "Đạt",
                "Đạt"
        );

        try {
            JsonNode payloadJson = objectMapper.valueToTree(payload);
            req.setDataPayload(payloadJson != null ? payloadJson : objectMapper.createObjectNode());
        } catch (Exception e) {
            throw new RuntimeException("Không thể chuyển đổi payload thành JsonNode", e);
        }

        req.setStudent(student);
        AIAnalysisRequest savedReq = requestRepository.save(req);

        // 5) Nếu có môn thiếu predicted_average -> không gọi AI, trả về result báo lỗi/thiếu
        if (!missingPredictedSubjects.isEmpty()) {
            AIAnalysisResult result = new AIAnalysisResult();
            result.setRequestId(savedReq.getRequestId());
            result.setGeneratedAt(LocalDateTime.now());

            // Build message tiếng Việt, liệt kê các môn
            String comment;
            if (missingPredictedSubjects.size() == 1 && "Không tìm thấy bất kỳ điểm nào cho học sinh này.".equals(missingPredictedSubjects.get(0))) {
                comment = "Không tìm thấy điểm nào để dự đoán cho học sinh.";
            } else {
                comment = "Thiếu điểm predicted_average cho các môn: " +
                        String.join(", ", missingPredictedSubjects) +
                        ". Vui lòng tính predicted_average trước khi yêu cầu dự đoán.";
            }

            result.setComment(comment);
            // Bạn có thể set other fields null/empty
            result.setPredictedAverage(null);
            result.setPredictedPerformance(null);
            result.setActualPerformance(null);
            result.setSuggestedActions(null);
            result.setDetailedAnalysis(null);
            result.setStatistics(null);

            AIAnalysisResult savedResult = resultRepository.save(result);
            return savedResult;
        }

        // 6) Nếu tất cả môn có predicted_average -> tiếp tục gọi AI như bình thường
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
            System.out.println("Tải trọng JSON được gửi đến AI: " + body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể tuần tự hóa tải trọng thành JSON", e);
        }
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // Gửi resp cho AI
        ResponseEntity<String> resp;
        String rawBody;
        try {
            resp = restTemplate.postForEntity(aiServiceUrl, entity, String.class);
            rawBody = resp.getBody();
            System.out.println("RAW AI response: " + rawBody);
        } catch (Exception ex) {
            AIAnalysisResult errorResult = new AIAnalysisResult();
            errorResult.setRequestId(savedReq.getRequestId());
            errorResult.setGeneratedAt(LocalDateTime.now());
            errorResult.setComment("Gọi Service AI không thành công: " + ex.getMessage() + ". Payload đã gửi: " + body);
            return resultRepository.save(errorResult);
        }

        if (rawBody == null || rawBody.isBlank()) {
            AIAnalysisResult errorResult = new AIAnalysisResult();
            errorResult.setRequestId(savedReq.getRequestId());
            errorResult.setGeneratedAt(LocalDateTime.now());
            errorResult.setComment("AI trả về Body rỗng");
            return resultRepository.save(errorResult);
        }

// Configure objectMapper if needed:
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        AiPredictResponse aiResp = null;
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode dataNode = root.path("data"); // .path không ném NPE
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                System.out.println("Không thấy node 'data' trong response.");
            } else {
                aiResp = objectMapper.treeToValue(dataNode, AiPredictResponse.class);
            }
        } catch (Exception ex) {
            System.err.println("Lỗi parse JSON từ AI: " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Mapped aiResp = " + aiResp);


        // 7) Lưu AIAnalysisResult từ response
        AIAnalysisResult result = new AIAnalysisResult();
        result.setRequestId(savedReq.getRequestId());
        if (aiResp != null) {
            Double predictedAvgDouble = null;
            if (aiResp.phan_tich_chi_tiet != null) {
                try {
                    Map<String, Object> map = objectMapper.convertValue(aiResp.phan_tich_chi_tiet, new TypeReference<Map<String, Object>>() {});
                    Object avgValue = map.get("diem_trung_binh");
                    predictedAvgDouble = parseNumberSafe(avgValue);
                } catch (Exception ex) {
                    System.err.println("Lỗi trích xuất diem_trung_binh: " + ex.getMessage());
                }
            }

            result.setPredictedAverage(predictedAvgDouble != null ? predictedAvgDouble.floatValue() : null);
            result.setPredictedPerformance(aiResp.du_doan_hoc_luc);
            result.setActualPerformance(aiResp.thuc_te_xep_loai);
            result.setComment(aiResp.goi_y_hanh_dong);
            result.setSuggestedActions(aiResp.goi_y_chi_tiet != null ? objectMapper.valueToTree(aiResp.goi_y_chi_tiet) : null);
            result.setDetailedAnalysis(aiResp.phan_tich_chi_tiet != null ? objectMapper.valueToTree(aiResp.phan_tich_chi_tiet) : null);
            result.setStatistics(aiResp.thong_ke != null ? objectMapper.valueToTree(aiResp.thong_ke) : null);
        } else {
            result.setComment("AI trả về Body rỗng");
        }
        result.setGeneratedAt(LocalDateTime.now());
        AIAnalysisResult savedResult = resultRepository.save(result);

        return savedResult;
    }

    // helper chuyển Object -> Double an toàn (giữ lại hoặc đặt vào class)
    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            String s = o.toString().trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return null;
        }
    }

    // helper parse number safe for AI response
    private Double parseNumberSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            String s = o.toString().trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Trích xuất giá trị từ một đối tượng phân tích chi tiết từ AI và chuyển đổi thành Map
     * @param analysisObj Đối tượng phân tích (có thể là Map, String JSON, hoặc đối tượng khác)
     * @param key Khóa cần trích xuất
     * @return Giá trị tương ứng với khóa, hoặc null nếu không tìm thấy
     */
    @SuppressWarnings("unchecked")
    private Object extractDoubleFromAnalysis(Object analysisObj, String key) {
        if (analysisObj == null) return null;

        try {
            // Nếu đã là Map, truy cập trực tiếp
            if (analysisObj instanceof Map) {
                return ((Map<String, Object>)analysisObj).get(key);
            }

            // Nếu là String (JSON), chuyển đổi thành Map
            if (analysisObj instanceof String) {
                Map<String, Object> map = objectMapper.readValue((String)analysisObj, Map.class);
                return map.get(key);
            }

            // Trường hợp khác, thử chuyển đổi thành Map
            Map<String, Object> map = objectMapper.convertValue(analysisObj, Map.class);
            return map.get(key);
        } catch (Exception ex) {
            System.err.println("Failed to extract " + key + " from analysis object: " + ex.getMessage());
            return null;
        }
    }
    //
    public AIAnalysisResult getLatestResultByStudentId(Long studentId,AnalysisType analysisType) {
        return resultRepository.findLatestResultByStudentId(studentId, analysisType.name());
    }
    public Optional<AIAnalysisResult> getLatestResult(Long studentId, String assignmentId) {
        return resultRepository.findLatestByStudentAndAssignment(studentId, assignmentId);
    }
}