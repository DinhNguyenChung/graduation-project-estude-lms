package org.example.estudebackendspring.mapper;

import org.example.estudebackendspring.dto.QuestionBankDTO;
import org.example.estudebackendspring.entity.Question;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper để chuyển đổi Question entity sang QuestionBankDTO
 * Tránh Jackson serialization issues với lazy loading
 */
@Component
public class QuestionBankMapper {
    
    /**
     * Chuyển Question entity sang QuestionBankDTO
     * Chỉ ánh xạ các trường đã được eager load
     */
    public QuestionBankDTO toDTO(Question question) {
        if (question == null) {
            return null;
        }
        
        QuestionBankDTO dto = QuestionBankDTO.builder()
                .questionId(question.getQuestionId())
                .questionText(question.getQuestionText())
                .points(question.getPoints())
                .questionType(question.getQuestionType())
                .difficultyLevel(question.getDifficultyLevel())
                .attachmentUrl(question.getAttachmentUrl())
                .build();
        
        // Map topic if loaded
        if (question.getTopic() != null) {
            QuestionBankDTO.SubjectInfo subjectInfo = null;
            if (question.getTopic().getSubject() != null) {
                subjectInfo = QuestionBankDTO.SubjectInfo.builder()
                        .subjectId(question.getTopic().getSubject().getSubjectId())
                        .name(question.getTopic().getSubject().getName())
                        .build();
            }
            
            dto.setTopic(QuestionBankDTO.TopicInfo.builder()
                    .topicId(question.getTopic().getTopicId())
                    .name(question.getTopic().getName())
                    .subject(subjectInfo)
                    .build());
        }
        
        // Map options if loaded
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            dto.setOptions(question.getOptions().stream()
                    .map(option -> QuestionBankDTO.QuestionOptionDTO.builder()
                            .optionId(option.getOptionId())
                            .optionText(option.getOptionText())
                            .isCorrect(option.getIsCorrect())
                            .optionOrder(option.getOptionOrder())
                            .build())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
