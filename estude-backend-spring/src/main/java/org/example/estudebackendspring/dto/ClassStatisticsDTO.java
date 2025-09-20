package org.example.estudebackendspring.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassStatisticsDTO {
    private Long classId;
    private String className;

    private Double classAverageScore;
    private Double submissionRate;
    private Double attendanceRate;

    private List<StudentStatisticsDTO> students; // chi tiết từng học sinh
}

