package org.example.estudebackendspring.dto;

import java.util.Map;

// Request payload gửi tới AI
public record AiPredictPayload(String student_id, Map<String, Float> grades,
                               Double ty_le_nop_bai, Double ty_le_nghi_hoc,
                               String the_duc, String qp_an) {}
