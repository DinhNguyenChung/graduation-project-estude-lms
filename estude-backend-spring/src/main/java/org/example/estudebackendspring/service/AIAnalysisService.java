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

//    @Value("${ai.service.url:http://127.0.0.1:8000/predict}")
    @Value("${ai.service.url}")
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
            String url = aiServiceUrl + "/predict";
            resp = restTemplate.postForEntity(url, entity, String.class);
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
    
    /**
     * Lấy AIAnalysisRequest theo requestId
     */
    public Optional<AIAnalysisRequest> getRequestById(Long requestId) {
        return requestRepository.findById(requestId);
    }
    
    /**
     * Lấy TẤT CẢ kết quả của student theo analysis type (ORDER BY mới nhất)
     */
    public List<AIAnalysisResult> getAllResultsByStudentIdAndType(Long studentId, AnalysisType analysisType) {
        return resultRepository.findAllByStudentIdAndAnalysisType(studentId, analysisType.name());
    }
    
    /**
     * Lấy TẤT CẢ kết quả theo student + assignment + analysis type
     */
    public List<AIAnalysisResult> getResultsByStudentAndAssignmentAndType(
            Long studentId, String assignmentId, AnalysisType analysisType) {
        return resultRepository.findAllByStudentAndAssignmentAndAnalysisType(
                studentId, assignmentId, analysisType.name());
    }
    
    public Optional<AIAnalysisResult> getLatestResult(Long studentId, String assignmentId) {
        return resultRepository.findLatestByStudentAndAssignment(studentId, assignmentId);
    }
    
    /**
     * Lấy roadmap tổng hợp (merged) từ TẤT CẢ các lần gọi Layer 5 của học sinh
     * Merge tất cả topics từ các roadmap để có đầy đủ topics
     */
    public AIAnalysisResult getMergedRoadmapByStudentId(Long studentId) {
        // Lấy tất cả roadmaps của học sinh
        List<AIAnalysisResult> allRoadmaps = getAllResultsByStudentIdAndType(
                studentId, AnalysisType.LEARNING_ROADMAP);
        
        if (allRoadmaps == null || allRoadmaps.isEmpty()) {
            return null;
        }
        
        // Nếu chỉ có 1 roadmap, trả về luôn
        if (allRoadmaps.size() == 1) {
            return allRoadmaps.get(0);
        }
        
        // Lấy roadmap mới nhất làm base
        AIAnalysisResult latestRoadmap = allRoadmaps.get(0);
        JsonNode latestData = latestRoadmap.getDetailedAnalysis();
        
        try {
            // Parse JSON thành Map để dễ xử lý
            Map<String, Object> mergedData = objectMapper.convertValue(
                    latestData, new TypeReference<Map<String, Object>>() {});
            
            // Lấy phases từ roadmap mới nhất
            List<Map<String, Object>> mergedPhases = (List<Map<String, Object>>) mergedData.get("phases");
            if (mergedPhases == null) {
                mergedPhases = new ArrayList<>();
            }
            
            // Tạo Set để track các topics đã có
            Set<String> existingTopics = new HashSet<>();
            for (Map<String, Object> phase : mergedPhases) {
                List<Map<String, Object>> topics = (List<Map<String, Object>>) phase.get("topics");
                if (topics != null) {
                    for (Map<String, Object> topic : topics) {
                        String topicName = (String) topic.get("topic");
                        if (topicName != null) {
                            existingTopics.add(topicName);
                        }
                    }
                }
            }
            
            // Merge topics từ các roadmap cũ hơn
            for (int i = 1; i < allRoadmaps.size(); i++) {
                AIAnalysisResult oldRoadmap = allRoadmaps.get(i);
                JsonNode oldData = oldRoadmap.getDetailedAnalysis();
                
                if (oldData == null || !oldData.has("phases")) {
                    continue;
                }
                
                Map<String, Object> oldDataMap = objectMapper.convertValue(
                        oldData, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> oldPhases = (List<Map<String, Object>>) oldDataMap.get("phases");
                
                if (oldPhases == null) {
                    continue;
                }
                
                // Duyệt qua các phases cũ
                for (Map<String, Object> oldPhase : oldPhases) {
                    List<Map<String, Object>> oldTopics = (List<Map<String, Object>>) oldPhase.get("topics");
                    
                    if (oldTopics == null) {
                        continue;
                    }
                    
                    // Tìm topics chưa có trong merged roadmap
                    List<Map<String, Object>> newTopicsToAdd = new ArrayList<>();
                    for (Map<String, Object> oldTopic : oldTopics) {
                        String topicName = (String) oldTopic.get("topic");
                        
                        if (topicName != null && !existingTopics.contains(topicName)) {
                            newTopicsToAdd.add(oldTopic);
                            existingTopics.add(topicName);
                        }
                    }
                    
                    // Nếu có topics mới, thêm phase mới hoặc merge vào phase tương ứng
                    if (!newTopicsToAdd.isEmpty()) {
                        // Tạo phase mới với topics từ roadmap cũ
                        Map<String, Object> newPhase = new HashMap<>(oldPhase);
                        newPhase.put("topics", newTopicsToAdd);
                        
                        // Update phase_number
                        int newPhaseNumber = mergedPhases.size() + 1;
                        newPhase.put("phase_number", newPhaseNumber);
                        
                        // Update daily_tasks day numbers
                        List<Map<String, Object>> dailyTasks = (List<Map<String, Object>>) newPhase.get("daily_tasks");
                        if (dailyTasks != null) {
                            int dayOffset = calculateTotalDays(mergedPhases);
                            for (Map<String, Object> dailyTask : dailyTasks) {
                                Integer currentDay = (Integer) dailyTask.get("day");
                                if (currentDay != null) {
                                    dailyTask.put("day", dayOffset + currentDay);
                                }
                            }
                        }
                        
                        mergedPhases.add(newPhase);
                    }
                }
            }
            
            // Update progress_tracking
            Map<String, Object> progressTracking = (Map<String, Object>) mergedData.get("progress_tracking");
            if (progressTracking != null) {
                progressTracking.put("total_phases", mergedPhases.size());
            }
            
            // Update estimated_completion_days
            int totalDays = calculateTotalDays(mergedPhases);
            mergedData.put("estimated_completion_days", totalDays);
            
            // Update phases
            mergedData.put("phases", mergedPhases);
            
            // Tạo AIAnalysisResult mới với merged data
            AIAnalysisResult mergedResult = new AIAnalysisResult();
            mergedResult.setResultId(latestRoadmap.getResultId());
            mergedResult.setRequestId(latestRoadmap.getRequestId());
            mergedResult.setGeneratedAt(latestRoadmap.getGeneratedAt());
            mergedResult.setComment("Merged roadmap from " + allRoadmaps.size() + " previous roadmaps");
            mergedResult.setDetailedAnalysis(objectMapper.valueToTree(mergedData));
            
            return mergedResult;
            
        } catch (Exception ex) {
            // Nếu có lỗi khi merge, trả về roadmap mới nhất
            return latestRoadmap;
        }
    }
    
    /**
     * Tính tổng số ngày từ danh sách phases
     */
    private int calculateTotalDays(List<Map<String, Object>> phases) {
        int totalDays = 0;
        for (Map<String, Object> phase : phases) {
            Integer durationDays = (Integer) phase.get("duration_days");
            if (durationDays != null) {
                totalDays += durationDays;
            }
        }
        return totalDays;
    }
    
    /**
     * Calculate progress từ roadmap JSON
     * Trả về: total_tasks, completed_tasks, total_phases, completed_phases, completion_percent
     */
    public Map<String, Object> calculateRoadmapProgress(JsonNode roadmapData) {
        int totalTasks = 0;
        int completedTasks = 0;
        int totalPhases = 0;
        int completedPhases = 0;
        
        JsonNode phases = roadmapData.get("phases");
        if (phases == null || !phases.isArray()) {
            return Map.of(
                "total_tasks", 0,
                "completed_tasks", 0,
                "total_phases", 0,
                "completed_phases", 0,
                "completion_percent", 0.0
            );
        }
        
        totalPhases = phases.size();
        
        for (JsonNode phase : phases) {
            JsonNode dailyTasks = phase.get("daily_tasks");
            if (dailyTasks == null || !dailyTasks.isArray()) {
                continue;
            }
            
            boolean allPhaseTasksComplete = true;
            
            for (JsonNode day : dailyTasks) {
                JsonNode tasks = day.get("tasks");
                if (tasks == null || !tasks.isArray()) {
                    continue;
                }
                
                for (JsonNode task : tasks) {
                    totalTasks++;
                    boolean completed = task.has("completed") && task.get("completed").asBoolean(false);
                    if (completed) {
                        completedTasks++;
                    } else {
                        allPhaseTasksComplete = false;
                    }
                }
            }
            
            if (allPhaseTasksComplete && totalTasks > 0) {
                completedPhases++;
            }
        }
        
        double completionPercent = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;
        
        return Map.of(
            "total_tasks", totalTasks,
            "completed_tasks", completedTasks,
            "total_phases", totalPhases,
            "completed_phases", completedPhases,
            "completion_percent", Math.round(completionPercent * 100.0) / 100.0
        );
    }
    
    /**
     * Get current phase (first phase with incomplete tasks)
     */
    public Map<String, Object> getCurrentPhase(JsonNode roadmapData) {
        JsonNode phases = roadmapData.get("phases");
        if (phases == null || !phases.isArray()) {
            return Map.of(
                "phase_number", 0,
                "phase_name", "N/A"
            );
        }
        
        for (JsonNode phase : phases) {
            boolean hasIncomplete = false;
            JsonNode dailyTasks = phase.get("daily_tasks");
            
            if (dailyTasks != null && dailyTasks.isArray()) {
                for (JsonNode day : dailyTasks) {
                    JsonNode tasks = day.get("tasks");
                    if (tasks != null && tasks.isArray()) {
                        for (JsonNode task : tasks) {
                            boolean completed = task.has("completed") && task.get("completed").asBoolean(false);
                            if (!completed) {
                                hasIncomplete = true;
                                break;
                            }
                        }
                    }
                    if (hasIncomplete) break;
                }
            }
            
            if (hasIncomplete) {
                return Map.of(
                    "phase_number", phase.has("phase_number") ? phase.get("phase_number").asInt() : 0,
                    "phase_name", phase.has("phase_name") ? phase.get("phase_name").asText() : "N/A"
                );
            }
        }
        
        // All phases completed hoặc không có tasks
        if (phases.size() > 0) {
            JsonNode lastPhase = phases.get(phases.size() - 1);
            return Map.of(
                "phase_number", lastPhase.has("phase_number") ? lastPhase.get("phase_number").asInt() : phases.size(),
                "phase_name", lastPhase.has("phase_name") ? lastPhase.get("phase_name").asText() : "Hoàn thành"
            );
        }
        
        return Map.of(
            "phase_number", 0,
            "phase_name", "N/A"
        );
    }
    
    /**
     * Get next incomplete tasks (limit số lượng)
     */
    public List<Map<String, Object>> getNextTasks(JsonNode roadmapData, int limit) {
        List<Map<String, Object>> nextTasks = new ArrayList<>();
        JsonNode phases = roadmapData.get("phases");
        
        if (phases == null || !phases.isArray()) {
            return nextTasks;
        }
        
        int count = 0;
        for (JsonNode phase : phases) {
            if (count >= limit) break;
            
            JsonNode dailyTasks = phase.get("daily_tasks");
            if (dailyTasks == null || !dailyTasks.isArray()) {
                continue;
            }
            
            String phaseName = phase.has("phase_name") ? phase.get("phase_name").asText() : "N/A";
            
            for (JsonNode day : dailyTasks) {
                if (count >= limit) break;
                
                JsonNode tasks = day.get("tasks");
                if (tasks == null || !tasks.isArray()) {
                    continue;
                }
                
                int dayNumber = day.has("day") ? day.get("day").asInt() : 0;
                
                for (JsonNode task : tasks) {
                    if (count >= limit) break;
                    
                    boolean completed = task.has("completed") && task.get("completed").asBoolean(false);
                    if (!completed) {
                        Map<String, Object> taskInfo = new HashMap<>();
                        taskInfo.put("task_id", task.has("task_id") ? task.get("task_id").asText() : "N/A");
                        taskInfo.put("title", task.has("title") ? task.get("title").asText() : "N/A");
                        taskInfo.put("type", task.has("type") ? task.get("type").asText() : "N/A");
                        taskInfo.put("duration_minutes", task.has("duration_minutes") ? task.get("duration_minutes").asInt() : 0);
                        taskInfo.put("day_number", dayNumber);
                        taskInfo.put("phase_name", phaseName);
                        
                        nextTasks.add(taskInfo);
                        count++;
                    }
                }
            }
        }
        
        return nextTasks;
    }
}