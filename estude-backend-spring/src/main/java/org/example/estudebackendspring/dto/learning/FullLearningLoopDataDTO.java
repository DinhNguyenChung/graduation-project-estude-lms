package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullLearningLoopDataDTO {
    @JsonProperty("layer1_feedback")
    private FeedbackDataDTO layer1Feedback;
    
    @JsonProperty("layer2_recommendation")
    private RecommendationDataDTO layer2Recommendation;
    
    @JsonProperty("layer3_practice_quiz")
    private PracticeQuizDataDTO layer3PracticeQuiz;
    
    @JsonProperty("next_step")
    private String nextStep;
}
