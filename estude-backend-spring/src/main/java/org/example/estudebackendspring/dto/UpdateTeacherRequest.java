package org.example.estudebackendspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating teacher of a ClassSubject
 * Used in PATCH /api/class-subjects/{classSubjectId}/teacher
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherRequest {
    
    /**
     * ID of the new teacher to assign
     * Can be null to remove the teacher assignment
     */
    @JsonProperty("teacherId")
    private Long teacherId;
}
