package org.example.estudebackendspring.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentRequest {
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    
    @NotEmpty(message = "At least one topic must be selected")
    private List<Long> topicIds;
    
    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "Number of questions must be at least 1")
    private Integer numQuestions;
    
    @NotNull(message = "Difficulty level is required")
    @Pattern(regexp = "easy|medium|hard|mixed", 
             message = "Difficulty must be: easy, medium, hard, or mixed")
    private String difficulty;
    
    @NotNull(message = "Grade level is required")
    @Pattern(regexp = "GRADE_10|GRADE_11|GRADE_12", 
             message = "Grade level must be: GRADE_10, GRADE_11, or GRADE_12")
    private String gradeLevel;
}
