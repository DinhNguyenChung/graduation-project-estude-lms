package org.example.estudebackendspring.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceQuestionDTO {
    private String question;
    private String topic;
    private String explanation;
}
