package org.example.estudebackendspring.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for marking a task as completed in a learning roadmap
 * Used in PUT /api/ai/me/roadmap/:id/task/:taskId/complete
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionRequest {
    
    @JsonProperty("actual_time_spent_minutes")
    private Integer actualTimeSpent;
    
    private Integer score;
    
    private Integer accuracy;
}
