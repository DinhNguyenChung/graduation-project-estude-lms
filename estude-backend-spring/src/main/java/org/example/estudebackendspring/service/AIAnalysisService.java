package org.example.estudebackendspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.estudebackendspring.dto.AiPredictPayload;
import org.example.estudebackendspring.dto.AiPredictResponse;
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
        // 1) Lấy student (để có studentCode)
        Student student = studentRepository.findById(studentUserId)
                .orElseThrow(() -> new NoSuchElementException("Student not found id=" + studentUserId));

        // 2) Lấy điểm thô từ repository (List<Object[]> như query native)
        List<Object[]> rows = studentRepository.findGradesByStudentId(studentUserId);
        // rows columns: 0:user_id,1:student_code,2:full_name,3:subject_name,4:midtermScore,5:finalScore,6:actualAverage,...

        // 3) Map subject -> score (ưu tiên actualAverage -> final -> midterm)
        Map<String, Double> gradesMap = new HashMap<>();
        for (Object[] r : rows) {
            String subjectName = r[3] != null ? r[3].toString() : "UNKNOWN";
            Double mid = toDouble(r[4]);
            Double fin = toDouble(r[5]);
            Double actual = toDouble(r[6]);
            Double score = actual != null ? actual : (fin != null ? fin : (mid != null ? mid : 0.0));
            gradesMap.put(subjectName, score);
        }

        // 4) Build payload - nếu bạn có thêm dữ liệu (ty_le_nop_bai, ty_le_nghi_hoc, the_duc, qp_an) -> lấy từ DB khác, còn thiếu thì đặt mặc định
        AiPredictPayload payload = new AiPredictPayload(
                student.getStudentCode(),
                gradesMap,
                95.0,         // ty_le_nop_bai (mặc định)
                2.0,          // ty_le_nghi_hoc (mặc định)
                "Đạt",        // the_duc
                "Đạt"         // qp_an
        );

        // 5) Lưu AIAnalysisRequest trước (chứa payload JSON)
        AIAnalysisRequest req = new AIAnalysisRequest();
        req.setRequestDate(LocalDateTime.now());
        req.setAnalysisType(AnalysisType.PREDICT_SEMESTER_PERFORMANCE); // chọn enum phù hợp
        JsonNode payloadJson = objectMapper.valueToTree(payload);
        req.setDataPayload(payloadJson != null ? payloadJson : objectMapper.createObjectNode());

        req.setStudent(student);
        AIAnalysisRequest savedReq = requestRepository.save(req);

        // 6) Gọi AI service
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<AiPredictResponse> aiResponseEntity;
        try {
            aiResponseEntity = restTemplate.postForEntity(aiServiceUrl, entity, AiPredictResponse.class);
        } catch (Exception ex) {
            // Nếu lỗi kết nối AI -> bạn có thể tạo AIAnalysisResult với thông báo lỗi để lưu
            AIAnalysisResult errorResult = new AIAnalysisResult();
            errorResult.setRequestId(savedReq.getRequestId());
            errorResult.setGeneratedAt(LocalDateTime.now());
            errorResult.setComment("AI service call failed: " + ex.getMessage());
            return resultRepository.save(errorResult);
        }

        AiPredictResponse aiResp = aiResponseEntity.getBody();

        // 7) Lưu AIAnalysisResult (dùng request_id để liên kết)
        AIAnalysisResult result = new AIAnalysisResult();
        result.setRequestId(savedReq.getRequestId());
        if (aiResp != null) {
            // Trích xuất predicted average từ response một cách an toàn
            Double predictedAvgDouble = null;
            if (aiResp.phan_tich_chi_tiet != null) {
                try {
                    // Nếu phan_tich_chi_tiet là Map => cast an toàn
                    if (aiResp.phan_tich_chi_tiet instanceof Map) {
                        Object avgValue = ((Map<?, ?>) aiResp.phan_tich_chi_tiet).get("diem_trung_binh");
                        predictedAvgDouble = parseNumberSafe(avgValue);
                    } else {
                        // Thử convert động (nếu là Object khác)
                        Map<String, Object> map = objectMapper.convertValue(aiResp.phan_tich_chi_tiet, Map.class);
                        Object avgValue = map.get("diem_trung_binh");
                        predictedAvgDouble = parseNumberSafe(avgValue);
                    }
                } catch (Exception ex) {
                    System.err.println("Error extracting diem_trung_binh: " + ex.getMessage());
                }
            }

            // Chuyển Double -> Float trước khi set (AIAnalysisResult.predictedAverage là Float)
            result.setPredictedAverage(predictedAvgDouble != null ? predictedAvgDouble.floatValue() : null);

            result.setPredictedPerformance(aiResp.du_doan_hoc_luc);
            result.setActualPerformance(aiResp.thuc_te_xep_loai);
            result.setComment(aiResp.goi_y_hanh_dong);
//            try {
            result.setSuggestedActions(aiResp.goi_y_chi_tiet != null ?
                    objectMapper.valueToTree(aiResp.goi_y_chi_tiet) : null);

            result.setDetailedAnalysis(aiResp.phan_tich_chi_tiet != null ?
                    objectMapper.valueToTree(aiResp.phan_tich_chi_tiet) : null);

            result.setStatistics(aiResp.thong_ke != null ?
                    objectMapper.valueToTree(aiResp.thong_ke) : null);
//            } catch (JsonProcessingException e) {
//                System.err.println("Error serializing AI response: " + e.getMessage());
//                if (result.getComment() == null) {
//                    result.setComment("Error processing AI response data");
//                }
//            }
        } else {
            result.setComment("AI returned null body");
        }
        result.setGeneratedAt(LocalDateTime.now());
        AIAnalysisResult savedResult = resultRepository.save(result);


        // 8) (Không cần set request -> mapping đã lưu request_id trên result)
        return savedResult;
    }

    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ex) { return null; }
    }

    private Double parseNumberSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ex) {
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
}