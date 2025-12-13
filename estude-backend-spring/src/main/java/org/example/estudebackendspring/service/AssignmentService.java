package org.example.estudebackendspring.service;

import org.example.estudebackendspring.dto.AssignmentResponseDTO;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.repository.AssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    // Create
    public Assignment createAssignment(Assignment assignment) {
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }

    // Read
    @Transactional(readOnly = true)
    public Assignment getAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        
        // Fetch questions with options separately to avoid CartesianProduct
        List<org.example.estudebackendspring.entity.Question> questions = 
            assignmentRepository.findQuestionsByAssignmentId(assignmentId);
        assignment.setQuestions(questions);
        
        return assignment;
    }

    // Update
    public Assignment updateAssignment(Long assignmentId, Assignment updated) {
        Assignment existing = getAssignment(assignmentId);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDueDate(updated.getDueDate());
        existing.setMaxScore(updated.getMaxScore());
        existing.setIsPublished(updated.getIsPublished());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setAllowLateSubmission(updated.getAllowLateSubmission());
        existing.setLatePenalty(updated.getLatePenalty());
        existing.setSubmissionLimit(updated.getSubmissionLimit());
        existing.setAnswerKeyFileUrl(updated.getAnswerKeyFileUrl());
        existing.setIsAutoGraded(updated.getIsAutoGraded());
        return assignmentRepository.save(existing);
    }

    // Delete
    public void deleteAssignment(Long assignmentId) {
        Assignment existing = getAssignment(assignmentId);
        if (existing.getSubmissions() != null && !existing.getSubmissions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete assignment with submissions");
        }
        assignmentRepository.delete(existing);
    }
    public List<Assignment> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findByClassId(classId);
    }
    public List<Assignment> getAssignmentsByClassSubject(Long subjectId) {
        return assignmentRepository.findAssignmentsByClassSubject_ClassSubjectId(subjectId);
    }
    
    /**
     * Convert Assignment entity to DTO to avoid lazy loading issues
     */
    @Transactional(readOnly = true)
    public AssignmentResponseDTO convertToDTO(Assignment assignment) {
        AssignmentResponseDTO dto = new AssignmentResponseDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setTimeLimit(assignment.getTimeLimit());
        dto.setType(assignment.getType());
        dto.setAttachmentUrl(assignment.getAttachmentUrl());
        dto.setMaxScore(assignment.getMaxScore());
        dto.setIsPublished(assignment.getIsPublished());
        dto.setAllowLateSubmission(assignment.getAllowLateSubmission());
        dto.setLatePenalty(assignment.getLatePenalty());
        dto.setSubmissionLimit(assignment.getSubmissionLimit());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        dto.setAnswerKeyFileUrl(assignment.getAnswerKeyFileUrl());
        dto.setIsAutoGraded(assignment.getIsAutoGraded());
        dto.setIsExam(assignment.getIsExam());
        dto.setStartDate(assignment.getStartDate());
        
        // Teacher info
        if (assignment.getTeacher() != null) {
            dto.setTeacherId(assignment.getTeacher().getUserId());
            dto.setTeacherName(assignment.getTeacher().getFullName());
        }
        
        // ClassSubject info
        if (assignment.getClassSubject() != null) {
            dto.setClassSubjectId(assignment.getClassSubject().getClassSubjectId());
            if (assignment.getClassSubject().getSubject() != null) {
                dto.setSubjectName(assignment.getClassSubject().getSubject().getName());
            }
            if (assignment.getClassSubject().getTerm() != null && 
                assignment.getClassSubject().getTerm().getClazz() != null) {
                dto.setClassName(assignment.getClassSubject().getTerm().getClazz().getName());
            }
        }
        
        return dto;
    }
    
    /**
     * Convert Assignment entity to AssignmentSummaryDTO with nested structure
     */
    @Transactional(readOnly = true)
    public org.example.estudebackendspring.dto.AssignmentSummaryDTO convertToSummaryDTO(Assignment a) {
        org.example.estudebackendspring.dto.AssignmentSummaryDTO dto = new org.example.estudebackendspring.dto.AssignmentSummaryDTO();
        dto.setAssignmentId(a.getAssignmentId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setDueDate(a.getDueDate());
        dto.setStartDate(a.getStartDate());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setType(a.getType());
        dto.setMaxScore(a.getMaxScore());
        dto.setTimeLimit(a.getTimeLimit());
        dto.setIsPublished(a.getIsPublished());
        dto.setAllowLateSubmission(a.getAllowLateSubmission());
        dto.setLatePenalty(a.getLatePenalty());
        dto.setSubmissionLimit(a.getSubmissionLimit());
        dto.setAttachmentUrl(a.getAttachmentUrl());
        dto.setIsAutoGraded(a.getIsAutoGraded());
        dto.setIsExam(a.getIsExam());
        
        // Map ClassSubject with nested DTOs
        if (a.getClassSubject() != null) {
            org.example.estudebackendspring.dto.ClassSubjectNestedDTO csDto = new org.example.estudebackendspring.dto.ClassSubjectNestedDTO();
            csDto.setClassSubjectId(a.getClassSubject().getClassSubjectId());
            
            // Map Subject
            if (a.getClassSubject().getSubject() != null) {
                org.example.estudebackendspring.dto.SubjectSimpleDTO subjectDto = new org.example.estudebackendspring.dto.SubjectSimpleDTO();
                subjectDto.setSubjectId(a.getClassSubject().getSubject().getSubjectId());
                subjectDto.setSubjectName(a.getClassSubject().getSubject().getName());
                subjectDto.setSubjectCode(a.getClassSubject().getSubject().getSubjectId().toString());
                csDto.setSubject(subjectDto);
            }
            
            // Map Clazz
            if (a.getClassSubject().getTerm() != null && a.getClassSubject().getTerm().getClazz() != null) {
                org.example.estudebackendspring.dto.ClazzSimpleDTO clazzDto = new org.example.estudebackendspring.dto.ClazzSimpleDTO();
                clazzDto.setClassId(a.getClassSubject().getTerm().getClazz().getClassId());
                clazzDto.setClassName(a.getClassSubject().getTerm().getClazz().getName());
                csDto.setClazz(clazzDto);
            }
            
            // Map Teacher
            if (a.getClassSubject().getTeacher() != null) {
                org.example.estudebackendspring.dto.TeacherSimpleDTO teacherDto = new org.example.estudebackendspring.dto.TeacherSimpleDTO();
                teacherDto.setTeacherId(a.getClassSubject().getTeacher().getUserId());
                teacherDto.setFullName(a.getClassSubject().getTeacher().getFullName());
                teacherDto.setAvatarPath(a.getClassSubject().getTeacher().getAvatarPath());
                csDto.setTeacher(teacherDto);
            }
            
            dto.setClassSubject(csDto);
        }
        
        // Map Topics from Questions
        if (a.getQuestions() != null && !a.getQuestions().isEmpty()) {
            List<org.example.estudebackendspring.dto.TopicSimpleDTO> topicDtos = a.getQuestions().stream()
                .filter(q -> q.getTopic() != null)
                .map(q -> {
                    org.example.estudebackendspring.dto.TopicSimpleDTO topicDto = new org.example.estudebackendspring.dto.TopicSimpleDTO();
                    topicDto.setTopicId(q.getTopic().getTopicId());
                    topicDto.setTopicName(q.getTopic().getName());
                    return topicDto;
                })
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            dto.setTopics(topicDtos);
        } else {
            dto.setTopics(java.util.Collections.emptyList());
        }
        
        // No status for class-subject endpoint (teacher view)
        dto.setStatus(null);
        
        return dto;
    }
    
    /**
     * Convert Assignment entity to AssignmentDetailNestedDTO with full details
     */
    @Transactional(readOnly = true)
    public org.example.estudebackendspring.dto.AssignmentDetailNestedDTO convertToDetailNestedDTO(Assignment a) {
        org.example.estudebackendspring.dto.AssignmentDetailNestedDTO dto = new org.example.estudebackendspring.dto.AssignmentDetailNestedDTO();
        dto.setAssignmentId(a.getAssignmentId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setDueDate(a.getDueDate());
        dto.setStartDate(a.getStartDate());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setType(a.getType());
        dto.setMaxScore(a.getMaxScore());
        dto.setTimeLimit(a.getTimeLimit());
        dto.setIsPublished(a.getIsPublished());
        dto.setAllowLateSubmission(a.getAllowLateSubmission());
        dto.setLatePenalty(a.getLatePenalty());
        dto.setSubmissionLimit(a.getSubmissionLimit());
        dto.setAttachmentUrl(a.getAttachmentUrl());
        
        // Map ClassSubject with nested DTOs
        if (a.getClassSubject() != null) {
            org.example.estudebackendspring.dto.ClassSubjectNestedDTO csDto = new org.example.estudebackendspring.dto.ClassSubjectNestedDTO();
            csDto.setClassSubjectId(a.getClassSubject().getClassSubjectId());
            
            // Map Subject
            if (a.getClassSubject().getSubject() != null) {
                org.example.estudebackendspring.dto.SubjectSimpleDTO subjectDto = new org.example.estudebackendspring.dto.SubjectSimpleDTO();
                subjectDto.setSubjectId(a.getClassSubject().getSubject().getSubjectId());
                subjectDto.setSubjectName(a.getClassSubject().getSubject().getName());
                subjectDto.setSubjectCode(a.getClassSubject().getSubject().getSubjectId().toString());
                csDto.setSubject(subjectDto);
            }
            
            // Map Clazz
            if (a.getClassSubject().getTerm() != null && a.getClassSubject().getTerm().getClazz() != null) {
                org.example.estudebackendspring.dto.ClazzSimpleDTO clazzDto = new org.example.estudebackendspring.dto.ClazzSimpleDTO();
                clazzDto.setClassId(a.getClassSubject().getTerm().getClazz().getClassId());
                clazzDto.setClassName(a.getClassSubject().getTerm().getClazz().getName());
                csDto.setClazz(clazzDto);
            }
            
            // Map Teacher
            if (a.getClassSubject().getTeacher() != null) {
                org.example.estudebackendspring.dto.TeacherSimpleDTO teacherDto = new org.example.estudebackendspring.dto.TeacherSimpleDTO();
                teacherDto.setTeacherId(a.getClassSubject().getTeacher().getUserId());
                teacherDto.setFullName(a.getClassSubject().getTeacher().getFullName());
                teacherDto.setAvatarPath(a.getClassSubject().getTeacher().getAvatarPath());
                csDto.setTeacher(teacherDto);
            }
            
            dto.setClassSubject(csDto);
        }
        
        // Map Topics from Questions
        if (a.getQuestions() != null && !a.getQuestions().isEmpty()) {
            List<org.example.estudebackendspring.dto.TopicSimpleDTO> topicDtos = a.getQuestions().stream()
                .filter(q -> q.getTopic() != null)
                .map(q -> {
                    org.example.estudebackendspring.dto.TopicSimpleDTO topicDto = new org.example.estudebackendspring.dto.TopicSimpleDTO();
                    topicDto.setTopicId(q.getTopic().getTopicId());
                    topicDto.setTopicName(q.getTopic().getName());
                    return topicDto;
                })
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            dto.setTopics(topicDtos);
        } else {
            dto.setTopics(java.util.Collections.emptyList());
        }
        
        // Map Questions with Options
        if (a.getQuestions() != null && !a.getQuestions().isEmpty()) {
            List<org.example.estudebackendspring.dto.QuestionDTO> questionDtos = a.getQuestions().stream()
                .map(q -> {
                    org.example.estudebackendspring.dto.QuestionDTO qDto = new org.example.estudebackendspring.dto.QuestionDTO();
                    qDto.setQuestionId(q.getQuestionId());
                    qDto.setQuestionText(q.getQuestionText());
                    qDto.setPoints(q.getPoints());
                    qDto.setQuestionType(q.getQuestionType() != null ? q.getQuestionType().name() : null);
                    qDto.setQuestionOrder(q.getQuestionOrder());
                    qDto.setAttachmentUrl(q.getAttachmentUrl());
                    
                    // Map options
                    if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                        List<org.example.estudebackendspring.dto.QuestionOptionDTO> optionDtos = q.getOptions().stream()
                            .map(opt -> {
                                org.example.estudebackendspring.dto.QuestionOptionDTO optDto = new org.example.estudebackendspring.dto.QuestionOptionDTO();
                                optDto.setOptionId(opt.getOptionId());
                                optDto.setOptionText(opt.getOptionText());
                                optDto.setOptionOrder(opt.getOptionOrder());
                                optDto.setIsCorrect(opt.getIsCorrect());
                                return optDto;
                            })
                            .collect(java.util.stream.Collectors.toList());
                        qDto.setOptions(optionDtos);
                    } else {
                        qDto.setOptions(java.util.Collections.emptyList());
                    }
                    
                    return qDto;
                })
                .collect(java.util.stream.Collectors.toList());
            dto.setQuestions(questionDtos);
        } else {
            dto.setQuestions(java.util.Collections.emptyList());
        }
        
        return dto;
    }
}