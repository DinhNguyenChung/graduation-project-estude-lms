package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectGradeRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long classSubjectId;

    // regularScores có thể rỗng (chưa có điểm thường)
    private List<@DecimalMin("0.0") @DecimalMax("10.0") Float> regularScores;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Float midtermScore;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Float finalScore;

    private String comment;
}
