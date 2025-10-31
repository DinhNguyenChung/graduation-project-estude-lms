package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapRequest {
    @JsonProperty("submission_id")
    private String submissionId;
    
    @JsonProperty("student_id")
    private Long studentId;
    
    private String subject;
    
    @JsonProperty("evaluation_data")
    private EvaluationDataDTO evaluationData;
    
    @JsonProperty("incorrect_questions")
    private List<IncorrectQuestionDTO> incorrectQuestions;
    
    @JsonProperty("learning_style")
    private String learningStyle;
    
    @JsonProperty("available_time_per_day")
    private Integer availableTimePerDay;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationDataDTO {
        private List<TopicEvaluationDTO> topics;
        
        @JsonProperty("overall_improvement")
        private OverallImprovementDTO overallImprovement;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicEvaluationDTO {
        private String topic;
        private Integer improvement;
        private String status;
        
        @JsonProperty("previous_accuracy")
        private Double previousAccuracy;
        
        @JsonProperty("new_accuracy")
        private Double newAccuracy;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallImprovementDTO {
        private Integer improvement;
        
        @JsonProperty("previous_average")
        private Double previousAverage;
        
        @JsonProperty("new_average")
        private Double newAverage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncorrectQuestionDTO {
        @JsonProperty("question_id")
        private Long questionId;
        
        private String topic;
        private String subtopic;
        private String difficulty;
        
        @JsonProperty("question_text")
        private String questionText;
        
        @JsonProperty("student_answer")
        private String studentAnswer;
        
        @JsonProperty("correct_answer")
        private String correctAnswer;
        
        @JsonProperty("error_type")
        private String errorType;
    }
}
