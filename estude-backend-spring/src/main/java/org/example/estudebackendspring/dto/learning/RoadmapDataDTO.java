package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapDataDTO {
    @JsonProperty("roadmap_id")
    private String roadmapId;
    
    @JsonProperty("student_id")
    private Long studentId;
    
    private String subject;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("estimated_completion_days")
    private Integer estimatedCompletionDays;
    
    @JsonProperty("overall_goal")
    private String overallGoal;
    
    private List<PhaseDTO> phases;
    
    @JsonProperty("progress_tracking")
    private ProgressTrackingDTO progressTracking;
    
    @JsonProperty("motivational_tips")
    private List<String> motivationalTips;
    
    @JsonProperty("adaptive_hints")
    private AdaptiveHintsDTO adaptiveHints;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhaseDTO {
        @JsonProperty("phase_number")
        private Integer phaseNumber;
        
        @JsonProperty("phase_name")
        private String phaseName;
        
        @JsonProperty("duration_days")
        private Integer durationDays;
        
        private String priority;
        
        private List<TopicDetailDTO> topics;
        
        @JsonProperty("daily_tasks")
        private List<DailyTaskDTO> dailyTasks;
        
        private MilestoneDTO milestone;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicDetailDTO {
        private String topic;
        private List<SubtopicDTO> subtopics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubtopicDTO {
        private String name;
        
        @JsonProperty("current_accuracy")
        private Double currentAccuracy;
        
        @JsonProperty("target_accuracy")
        private Integer targetAccuracy;
        
        @JsonProperty("focus_areas")
        private List<String> focusAreas;
        
        @JsonProperty("learning_resources")
        private List<LearningResourceDTO> learningResources;
        
        @JsonProperty("incorrect_questions_review")
        private List<Long> incorrectQuestionsReview;
        
        @JsonProperty("practice_exercises")
        private Integer practiceExercises;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningResourceDTO {
        private String type;
        private String title;
        private String url;
        private String description;
        
        @JsonProperty("estimated_time_minutes")
        private Integer estimatedTimeMinutes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTaskDTO {
        private Integer day;
        private List<TaskDTO> tasks;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDTO {
        @JsonProperty("task_id")
        private String taskId;
        
        private String type;
        private String title;
        
        @JsonProperty("duration_minutes")
        private Integer durationMinutes;
        
        private Boolean completed;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneDTO {
        private String name;
        private String criteria;
        
        @JsonProperty("assessment_type")
        private String assessmentType;
        
        @JsonProperty("num_questions")
        private Integer numQuestions;
        
        private List<String> topics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressTrackingDTO {
        @JsonProperty("completed_phases")
        private Integer completedPhases;
        
        @JsonProperty("total_phases")
        private Integer totalPhases;
        
        @JsonProperty("completion_percent")
        private Integer completionPercent;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdaptiveHintsDTO {
        @JsonProperty("learning_style_match")
        private String learningStyleMatch;
        
        @JsonProperty("time_management")
        private String timeManagement;
        
        @JsonProperty("difficulty_progression")
        private String difficultyProgression;
    }
}
