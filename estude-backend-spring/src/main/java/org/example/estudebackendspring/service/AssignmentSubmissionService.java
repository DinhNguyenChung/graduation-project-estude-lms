package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AnswerType;
import org.example.estudebackendspring.enums.SubmissionStatus;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssignmentSubmissionService {

    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AnswerRepository answerRepository;
    private final GradeRepository gradeRepository;

    public AssignmentSubmissionService(EnrollmentRepository enrollmentRepository,
                                       AssignmentRepository assignmentRepository,
                                       QuestionRepository questionRepository,
                                       StudentRepository studentRepository,
                                       SubmissionRepository submissionRepository,
                                       QuestionOptionRepository questionOptionRepository,
                                       AnswerRepository answerRepository,
                                       GradeRepository gradeRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.questionRepository = questionRepository;
        this.studentRepository = studentRepository;
        this.submissionRepository = submissionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.answerRepository = answerRepository;
        this.gradeRepository = gradeRepository;
    }

    /* 1) List assignments for student (classes student enrolled in) */
    public List<AssignmentSummaryDTO> listAssignmentsForStudent(Long studentId) {
        List<Long> classIds = enrollmentRepository.findClassIdsByStudentId(studentId);
        if (classIds == null || classIds.isEmpty()) return Collections.emptyList();
        List<Assignment> assignments = assignmentRepository.findPublishedByClassIds(classIds);

        return assignments.stream().map(a -> {
            AssignmentSummaryDTO dto = new AssignmentSummaryDTO();
            dto.setAssignmentId(a.getAssignmentId());
            dto.setTitle(a.getTitle());
            dto.setDueDate(a.getDueDate());
            if (a.getClassSubject() != null && a.getClassSubject().getClazz() != null) {
                dto.setClassId(a.getClassSubject().getClazz().getClassId());
                dto.setClassName(a.getClassSubject().getClazz().getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /* 2) Assignment detail */
    public AssignmentDetailDTO getAssignmentDetail(Long assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        AssignmentDetailDTO dto = new AssignmentDetailDTO();
        dto.setAssignmentId(a.getAssignmentId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setDueDate(a.getDueDate());

        List<Question> questions = questionRepository.findByAssignmentAssignmentIdOrderByQuestionOrder(assignmentId);
        List<QuestionDTO> qdto = questions.stream().map(q -> {
            QuestionDTO qd = new QuestionDTO();
            qd.setQuestionId(q.getQuestionId());
            qd.setQuestionText(q.getQuestionText());
            qd.setPoints(q.getPoints());
            qd.setQuestionType(q.getQuestionType() != null ? q.getQuestionType().name() : null);
            if (q.getOptions() != null) {
                qd.setOptions(q.getOptions().stream().map(opt -> {
                    QuestionOptionDTO pod = new QuestionOptionDTO();
                    pod.setOptionId(opt.getOptionId());
                    pod.setOptionText(opt.getOptionText());
                    pod.setIsCorrect(opt.getIsCorrect()); // if you want to hide correct flag for students, remove this line
                    return pod;
                }).collect(Collectors.toList()));
            }
            return qd;
        }).collect(Collectors.toList());

        dto.setQuestions(qdto);
        return dto;
    }

    /* 3) Submit assignment (multipart/form-data: submission JSON + optional files) */
    @Transactional
    public SubmissionResultDTO submitAssignment(SubmissionRequest req, List<MultipartFile> files) {
        // validate
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.getStudentId()));
        Assignment assignment = assignmentRepository.findById(req.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + req.getAssignmentId()));

        // ensure student is enrolled in assignment's class
        Long classId = Optional.ofNullable(assignment.getClassSubject())
                .map(cs -> cs.getClazz().getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment has no class assigned"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndClassId(student.getUserId(), classId);
        if (!enrolled) throw new ResourceNotFoundException("Student is not enrolled in the class for this assignment");

        // create submission
        Submission sub = new Submission();
        sub.setSubmittedAt(LocalDateTime.now());
        sub.setAssignment(assignment);
        sub.setStudent(student);
        sub.setStatus(SubmissionStatus.SUBMITTED);
        sub.setContent(req.getContent());

        // handle files (example: store filenames as comma-separated) - replace with real storage
        if (files != null && !files.isEmpty()) {
            String fileNames = files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining(","));
            sub.setFileUrl(fileNames);
        }

        sub = submissionRepository.save(sub);

        // process answers and auto-grade option questions
        int totalQ = 0;
        int correctCount = 0;
        float totalScore = 0f;

        List<AnswerRequest> answers = req.getAnswers() == null ? Collections.emptyList() : req.getAnswers();

        for (AnswerRequest ar : answers) {
            totalQ++;
            Question q = questionRepository.findById(ar.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + ar.getQuestionId()));

            Answer ans = new Answer();
            ans.setSubmission(sub);
            ans.setQuestion(q);
            ans.setStudentAnswerText(ar.getTextAnswer());
            ans.setAnswerType(q.getQuestionType() != null ? mapQuestionTypeToAnswerType(q.getQuestionType()) : null);
            // link chosen option if provided
            if (ar.getChosenOptionId() != null) {
                questionOptionRepository.findById(ar.getChosenOptionId()).ifPresent(ans::setChosenOption);
            }
            // auto grade for OPTION type
            boolean isCorrect = false;
            float qScore = 0f;
            if (q.getQuestionType() != null && q.getQuestionType().name().equalsIgnoreCase("OPTION")) {
                // find chosen option and check .isCorrect
                if (ar.getChosenOptionId() != null) {
                    QuestionOption opt = questionOptionRepository.findById(ar.getChosenOptionId()).orElse(null);
                    if (opt != null && Boolean.TRUE.equals(opt.getIsCorrect())) {
                        isCorrect = true;
                        qScore = q.getPoints() != null ? q.getPoints() : 0f;
                    }
                }
            }
            // TEXT questions cannot be reliably auto-graded without an answer key -> leave isCorrect null/false
            ans.setIsCorrect(isCorrect);
            ans.setScore(qScore);
            ans.setFeedback(isCorrect ? "Correct" : "Pending manual review");
            ans = answerRepository.save(ans);

            // accumulate
            if (isCorrect) correctCount++;
            totalScore += qScore;
        }

        // create grade (auto-graded summary)
        Grade grade = new Grade();
        grade.setScore(totalScore);
        grade.setGradedAt(LocalDateTime.now());
        grade.setIsAutoGraded(true);
        grade.setAutoGradedFeedback(generateAiFeedbackPlaceholder(correctCount, totalQ));
        grade.setSubmission(sub);
        grade = gradeRepository.save(grade);

        // link and persist submission's grade
        sub.setGrade(grade);
        sub = submissionRepository.save(sub);

        SubmissionResultDTO result = new SubmissionResultDTO();
        result.setSubmissionId(sub.getSubmissionId());
        result.setCorrectCount(correctCount);
        result.setTotalQuestions(totalQ);
        result.setScore(totalScore);
        result.setAiFeedback(grade.getAutoGradedFeedback());

        return result;
    }

    private AnswerType mapQuestionTypeToAnswerType(Enum questionTypeEnum) {
        // Convert your QuestionType to AnswerType mapping (simple heuristic).
        // If QuestionType has values like OPTION, TEXT, FILE, map accordingly.
        String s = questionTypeEnum.name();
        if (s.equalsIgnoreCase("OPTION")) return AnswerType.OPTION;
        if (s.equalsIgnoreCase("TEXT") || s.equalsIgnoreCase("ESSAY")) return AnswerType.TEXT;
        return AnswerType.TEXT;
    }

    /* 4) Get submission detail */
    public SubmissionDetailDTO getSubmissionDetail(Long submissionId) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        SubmissionDetailDTO dto = new SubmissionDetailDTO();
        dto.setSubmissionId(sub.getSubmissionId());
        dto.setSubmittedAt(sub.getSubmittedAt());
        dto.setStatus(sub.getStatus() != null ? sub.getStatus().name() : null);
        dto.setScore(sub.getGrade() != null ? sub.getGrade().getScore() : null);
        dto.setAiFeedback(sub.getGrade() != null ? sub.getGrade().getAutoGradedFeedback() : null);

        List<AnswerDetailDTO> ad = Optional.ofNullable(sub.getAnswers()).orElse(Collections.emptyList()).stream().map(a -> {
            AnswerDetailDTO x = new AnswerDetailDTO();
            x.setAnswerId(a.getAnswerId());
            x.setQuestionId(a.getQuestion() != null ? a.getQuestion().getQuestionId() : null);
            x.setStudentAnswerText(a.getStudentAnswerText());
            x.setIsCorrect(a.getIsCorrect());
            x.setScore(a.getScore());
            x.setFeedback(a.getFeedback());
            return x;
        }).collect(Collectors.toList());

        dto.setAnswers(ad);
        return dto;
    }

    private String generateAiFeedbackPlaceholder(int correctCount, int total) {
        if (total == 0) return "No questions to grade.";
        float pct = 100f * correctCount / total;
        return String.format("Bạn trả lời đúng %d/%d (%.1f%%). Xem phần giải thích để cải thiện.", correctCount, total, pct);
    }
}
