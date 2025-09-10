package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.SubmissionRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

//    public List<Submission> getSubmissionsByClassSubject(Long classSubjectId) {
//        return submissionRepository.findByAssignment_ClassSubject_ClassSubjectId(classSubjectId);
//    }
public List<SubmissionResponseDTO> getSubmissionsByClassSubject(Long classSubjectId) {
    List<Submission> submissions =
            submissionRepository.findByAssignment_ClassSubject_ClassSubjectId(classSubjectId);

    return submissions.stream().map(s -> {
        SubmissionResponseDTO dto = new SubmissionResponseDTO();
        // submission
        dto.setSubmissionId(s.getSubmissionId());
        dto.setSubmittedAt(s.getSubmittedAt());
        dto.setFileUrl(s.getFileUrl());
        dto.setContent(s.getContent());
        dto.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
        dto.setIsLate(s.getIsLate());
        dto.setAttemptNumber(s.getAttemptNumber());
        dto.setAutoGradedAt(s.getAutoGradedAt());

        // student
        if (s.getStudent() != null) {
            dto.setStudentId(s.getStudent().getUserId());
            dto.setStudentCode(s.getStudent().getStudentCode());
            dto.setStudentName(s.getStudent().getFullName());
        }

        // assignment
        if (s.getAssignment() != null) {
            dto.setAssignmentId(s.getAssignment().getAssignmentId());
            dto.setAssignmentTitle(s.getAssignment().getTitle());
            dto.setDueDate(s.getAssignment().getDueDate());
        }

        // grade
        if (s.getGrade() != null) {
            dto.setGradeId(s.getGrade().getGradeId());
            dto.setScore(s.getGrade().getScore());
            dto.setGradeComment(s.getGrade().getAutoGradedFeedback());
        }

        return dto;
    }).collect(Collectors.toList());
}
    @Transactional
    public Optional<SubmissionDTO> getSubmission(Long submissionId) {
        return submissionRepository.findBySubmissionId(submissionId)
                .map(this::convertToSubmissionDTO);
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }
    public AssignmentDTO getAssignmentBySubmission(Long submissionId) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        Assignment a = sub.getAssignment();
        if (a == null) {
            throw new ResourceNotFoundException("Assignment not found for submission: " + submissionId);
        }

        return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueDate()
        );
    }


    @Transactional
    public List<SubmissionDTO> getSubmissionsByStudentAndAssignment(Long studentId, Long assignmentId) {
        if (studentId == null || studentId <= 0 || assignmentId == null || assignmentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID or assignment ID");
        }
        List<Submission> submissions = submissionRepository.findByStudent_userIdAndAssignment_AssignmentId(studentId, assignmentId);
        return submissions.stream()
                .map(this::convertToSubmissionDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public List<SubmissionDTO> getSubmissionsByStudent(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        List<Submission> submissions = submissionRepository.findByStudent_userId(studentId);
        return submissions.stream()
                .map(this::convertToSubmissionDTO)
                .collect(Collectors.toList());
    }

    private SubmissionDTO convertToSubmissionDTO(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setFileUrl(submission.getFileUrl());
        dto.setContent(submission.getContent());
        dto.setStatus(submission.getStatus());
        dto.setIsLate(submission.getIsLate());
        dto.setAttemptNumber(submission.getAttemptNumber());
        dto.setAutoGradedAt(submission.getAutoGradedAt());
        dto.setClassSubjectId(submission.getAssignment().getClassSubject().getClassSubjectId());
        dto.setSubjectName(submission.getAssignment().getClassSubject().getSubject().getName());
        dto.setClassName(submission.getAssignment().getClassSubject().getClazz().getName());
        dto.setGradeId(submission.getGrade().getGradeId());
        dto.setScore(submission.getGrade().getScore());
        dto.setAutoGradeFeedback(submission.getGrade().getAutoGradedFeedback());
        if (submission.getStudent() != null) {
            dto.setStudentId(submission.getStudent().getUserId());
        }
        if (submission.getAssignment() != null) {
            dto.setAssignmentId(submission.getAssignment().getAssignmentId());
            dto.setAssignmentName(submission.getAssignment().getTitle());
        }

        List<AnswerDTO> answerDTOs = submission.getAnswers().stream()
                .map(this::convertToAnswerDTO)
                .collect(Collectors.toList());
        dto.setAnswers(answerDTOs);

        return dto;
    }

    private AnswerDTO convertToAnswerDTO(Answer answer) {
        AnswerDTO dto = new AnswerDTO();
        dto.setAnswerId(answer.getAnswerId());
        dto.setStudentAnswerText(answer.getStudentAnswerText());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setAnswerType(answer.getAnswerType());
        dto.setFileUrl(answer.getFileUrl());
        dto.setScore(answer.getScore());
        dto.setFeedback(answer.getFeedback());

        if (answer.getQuestion() != null) {
            dto.setQuestion(convertToQuestionAswerDTO(answer.getQuestion()));
        }
        if (answer.getChosenOption() != null) {
            dto.setChosenOption(convertToQuestionOptionAnswerDTO(answer.getChosenOption()));
        }

        return dto;
    }

    private QuestionAswerDTO convertToQuestionAswerDTO(Question question) {
        QuestionAswerDTO dto = new QuestionAswerDTO();
        dto.setQuestionId(question.getQuestionId());
        dto.setQuestionText(question.getQuestionText());
        dto.setPoints(question.getPoints());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setAttachmentUrl(question.getAttachmentUrl());

        if (question.getAssignment() != null) {
            dto.setAssignmentId(question.getAssignment().getAssignmentId());
        }

        List<QuestionOptionAnswerDTO> optionDTOs = question.getOptions().stream()
                .map(this::convertToQuestionOptionAnswerDTO)
                .collect(Collectors.toList());
        dto.setOptions(optionDTOs);

        return dto;
    }

    private QuestionOptionAnswerDTO convertToQuestionOptionAnswerDTO(QuestionOption option) {
        QuestionOptionAnswerDTO dto = new QuestionOptionAnswerDTO();
        dto.setOptionId(option.getOptionId());
        dto.setOptionText(option.getOptionText());
        dto.setIsCorrect(option.getIsCorrect());
        dto.setOptionOrder(option.getOptionOrder());
        dto.setExplanation(option.getExplanation());

        if (option.getQuestion() != null) {
            dto.setQuestionId(option.getQuestion().getQuestionId());
        }

        return dto;
    }

}