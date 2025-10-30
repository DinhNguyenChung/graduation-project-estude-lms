package org.example.estudebackendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubjectDTO {
    private Long subjectId;
    private String name;
    private String description;
    // Note: gradeLevel and volume removed from Subject
    // These are now managed at Topic level for more flexibility
    // One Subject (e.g., "Toán") can have topics for multiple grades and volumes

    // Constructor cũ để tương thích với code hiện tại
    public SubjectDTO(Long subjectId, String name, String description) {
        this.subjectId = subjectId;
        this.name = name;
        this.description = description;
    }
}

