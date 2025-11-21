package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadmapDataDTO {
    // BE-generated field để FE có thể reference kết quả này
    @JsonProperty("result_id")
    private Long resultId;
    
    @JsonProperty("roadmap_id")
    private String roadmapId;
    
    @JsonProperty("student_id")
    private Long studentId;
    
    private String subject;
    
    @JsonProperty("generated_by")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String generatedBy;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("estimated_completion_days")
    @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
    private Integer estimatedCompletionDays;
    
    @JsonProperty("overall_goal")
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String overallGoal;
    
    private List<PhaseDTO> phases;
    
    @JsonProperty("progress_tracking")
    private ProgressTrackingDTO progressTracking;
    
    @JsonProperty("motivational_tips")
    private List<String> motivationalTips;
    
    @JsonProperty("adaptive_hints")
    private AdaptiveHintsDTO adaptiveHints;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PhaseDTO {
        @JsonProperty("phase_number")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer phaseNumber;
        
        @JsonProperty("phase_name")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String phaseName;
        
        @JsonProperty("duration_days")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer durationDays;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String priority;
        
        private List<TopicDetailDTO> topics;
        
        @JsonProperty("daily_tasks")
        private List<DailyTaskDTO> dailyTasks;
        
        private MilestoneDTO milestone;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicDetailDTO {
        private String topic;
        private List<SubtopicDTO> subtopics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubtopicDTO {
        private String name;
        
        @JsonProperty("current_accuracy")
        private Double currentAccuracy;
        
        @JsonProperty("target_accuracy")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer targetAccuracy;
        
        @JsonProperty("focus_areas")
        private List<String> focusAreas;
        
        @JsonProperty("learning_resources")
        private List<LearningResourceDTO> learningResources;
        
        @JsonProperty("incorrect_questions_review")
        private List<IncorrectQuestionReviewDTO> incorrectQuestionsReview;
        
        @JsonProperty("practice_exercises")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer practiceExercises;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LearningResourceDTO {
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String type;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String title;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String url;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String description;
        
        @JsonProperty("estimated_time_minutes")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer estimatedTimeMinutes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncorrectQuestionReviewDTO {
        @JsonProperty("question_id")
        private Long questionId;
        
        @JsonProperty("question_text")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String questionText;
        
        @JsonProperty("your_answer")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String yourAnswer;
        
        @JsonProperty("correct_answer")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String correctAnswer;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String explanation;
        
        @JsonProperty("common_mistake")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String commonMistake;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String tip;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyTaskDTO {
        private Integer day;
        private List<TaskDTO> tasks;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskDTO {
        @JsonProperty("task_id")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String taskId;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String type;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String title;
        
        @JsonProperty("duration_minutes")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer durationMinutes;
        
        private Boolean completed;
        
        // Fields for LEARN type
        @JsonProperty("learning_summary")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String learningSummary;
        
        @JsonProperty("theory_explanation")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String theoryExplanation;
        
        @JsonProperty("key_points")
        private List<String> keyPoints;
        
        private ExampleDTO example;
        
        @JsonProperty("recommended_resources")
        private List<LearningResourceDTO> recommendedResources;
        
        // Fields for PRACTICE type
        @JsonProperty("practice_set")
        private List<PracticeQuestionDTO> practiceSet;
        
        @JsonProperty("focus_skill")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String focusSkill;
        
        @JsonProperty("expected_accuracy")
        private Double expectedAccuracy;
        
        // Common fields for all types
        @JsonProperty("related_subtopic")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String relatedSubtopic;
        
        @JsonProperty("target_skill")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String targetSkill;
        
        @JsonProperty("difficulty_level")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer difficultyLevel;
        
        @JsonProperty("estimated_learning_outcome")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String estimatedLearningOutcome;
        
        @JsonProperty("learning_style_support")
        private List<String> learningStyleSupport;
        
        private List<String> tips;
        
        // Progress tracking fields
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String status;
        
        @JsonProperty("actual_time_spent_minutes")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer actualTimeSpentMinutes;
        
        private Double score;
        private Double accuracy;
        
        @JsonProperty("completed_at")
        private LocalDateTime completedAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MilestoneDTO {
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String name;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String criteria;
        
        @JsonProperty("assessment_type")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String assessmentType;
        
        @JsonProperty("num_questions")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer numQuestions;
        
        private List<String> topics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProgressTrackingDTO {
        @JsonProperty("completed_phases")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer completedPhases;
        
        @JsonProperty("total_phases")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer totalPhases;
        
        @JsonProperty("completion_percent")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer completionPercent;
        
        @JsonProperty("total_tasks")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer totalTasks;
        
        @JsonProperty("completed_tasks")
        @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
        private Integer completedTasks;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdaptiveHintsDTO {
        @JsonProperty("learning_style_match")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String learningStyleMatch;
        
        @JsonProperty("time_management")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String timeManagement;
        
        @JsonProperty("difficulty_progression")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String difficultyProgression;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExampleDTO {
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String question;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String solution;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PracticeQuestionDTO {
        @JsonProperty("question_id")
        private Long questionId;
        
        @JsonProperty("question_text")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String questionText;
        
        private List<String> choices;
        
        @JsonProperty("correct_answer")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String correctAnswer;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String explanation;
        
        @JsonProperty("related_theory")
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String relatedTheory;
        
        @JsonDeserialize(using = FlexibleStringDeserializer.class)
        private String difficulty;
    }
}
