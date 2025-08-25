package org.example.estudebackendspring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.repository.SubmissionReportRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubmissionReportService {

    private final SubmissionReportRepository submissionReportRepository;
    private final ObjectMapper objectMapper;

    public JsonNode getStudentSubmission(Long assignmentId, Long studentId) {
        Optional<String> resultJsonOpt = submissionReportRepository
                .getStudentSubmissionJson(assignmentId, studentId);

        if (resultJsonOpt.isEmpty()) {
            // Không có dữ liệu
            return objectMapper.createObjectNode()
                    .put("message", "Không tìm thấy bài làm cho học sinh này");
        }

        try {
            return objectMapper.readTree(resultJsonOpt.get());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing submission JSON", e);
        }
    }
}
