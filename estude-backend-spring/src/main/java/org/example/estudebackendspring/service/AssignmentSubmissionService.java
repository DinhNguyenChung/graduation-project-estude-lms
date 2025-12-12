package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AnswerType;
import org.example.estudebackendspring.enums.QuestionType;
import org.example.estudebackendspring.enums.SubmissionStatus;
import org.example.estudebackendspring.exception.LateSubmissionNotAllowedException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.exception.SubmissionLimitExceededException;
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AssignmentSummaryDTO> listAssignmentsForStudent(Long studentId) {
        List<Long> classIds = enrollmentRepository.findClassIdsByStudentId(studentId);
        if (classIds == null || classIds.isEmpty()) return Collections.emptyList();
        List<Assignment> assignments = assignmentRepository.findPublishedByClassIds(classIds);

        return assignments.stream().map(a -> {
            AssignmentSummaryDTO dto = new AssignmentSummaryDTO();
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
                ClassSubjectNestedDTO csDto = new ClassSubjectNestedDTO();
                csDto.setClassSubjectId(a.getClassSubject().getClassSubjectId());
                
                // Map Subject
                if (a.getClassSubject().getSubject() != null) {
                    SubjectSimpleDTO subjectDto = new SubjectSimpleDTO();
                    subjectDto.setSubjectId(a.getClassSubject().getSubject().getSubjectId());
                    subjectDto.setSubjectName(a.getClassSubject().getSubject().getName());
                    subjectDto.setSubjectCode(a.getClassSubject().getSubject().getSubjectId().toString());
                    csDto.setSubject(subjectDto);
                }
                
                // Map Clazz
                if (a.getClassSubject().getTerm() != null && a.getClassSubject().getTerm().getClazz() != null) {
                    ClazzSimpleDTO clazzDto = new ClazzSimpleDTO();
                    clazzDto.setClassId(a.getClassSubject().getTerm().getClazz().getClassId());
                    clazzDto.setClassName(a.getClassSubject().getTerm().getClazz().getName());
                    csDto.setClazz(clazzDto);
                }
                
                // Map Teacher
                if (a.getClassSubject().getTeacher() != null) {
                    TeacherSimpleDTO teacherDto = new TeacherSimpleDTO();
                    teacherDto.setTeacherId(a.getClassSubject().getTeacher().getUserId());
                    teacherDto.setFullName(a.getClassSubject().getTeacher().getFullName());
                    teacherDto.setAvatarPath(a.getClassSubject().getTeacher().getAvatarPath());
                    csDto.setTeacher(teacherDto);
                }
                
                dto.setClassSubject(csDto);
            }
            
            // Map Topics from Questions
            if (a.getQuestions() != null && !a.getQuestions().isEmpty()) {
                List<TopicSimpleDTO> topicDtos = a.getQuestions().stream()
                    .filter(q -> q.getTopic() != null)
                    .map(q -> {
                        TopicSimpleDTO topicDto = new TopicSimpleDTO();
                        topicDto.setTopicId(q.getTopic().getTopicId());
                        topicDto.setTopicName(q.getTopic().getName());
                        return topicDto;
                    })
                    .distinct()
                    .collect(Collectors.toList());
                dto.setTopics(topicDtos);
            } else {
                dto.setTopics(Collections.emptyList());
            }
            
            // Set status based on submissions
            if (a.getSubmissions() != null && !a.getSubmissions().isEmpty()) {
                dto.setStatus(a.getSubmissions().stream()
                    .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                    .map(s -> s.getStatus() != null ? s.getStatus().toString() : "NOT_SUBMITTED")
                    .findFirst()
                    .orElse("NOT_SUBMITTED"));
            } else {
                dto.setStatus("NOT_SUBMITTED");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /* 2) Assignment detail */
    public AssignmentDetailDTO getAssignmentDetail(Long assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập: " + assignmentId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh: " + req.getStudentId()));
        Assignment assignment = assignmentRepository.findById(req.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập: " + req.getAssignmentId()));

        // ensure student is enrolled in assignment's class
        Long classId = Optional.ofNullable(assignment.getClassSubject())
                .map(cs -> cs.getTerm().getClazz().getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Bài tập không có lớp nào được chỉ định"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndClassId(student.getUserId(), classId);
        if (!enrolled) throw new ResourceNotFoundException("Học sinh không được đăng ký vào lớp cho bài tập này");

        // ----- NEW: check submission limit -----
        Integer submissionLimit = assignment.getSubmissionLimit(); // may be null
        int existingCount = submissionRepository.countByAssignmentAndStudent(assignment, student);
        if (submissionLimit != null && existingCount >= submissionLimit) {
            throw new SubmissionLimitExceededException("Đã đạt đến giới hạn nộp bài tập này (giới hạn=" + submissionLimit + ")");
        }

        // ----- NEW: check due date / late submission -----
        LocalDateTime now = LocalDateTime.now();
        boolean isLate = false;
        if (assignment.getDueDate() != null && now.isAfter(assignment.getDueDate())) {
            // it's after due date
            if (assignment.getAllowLateSubmission() == null || Boolean.FALSE.equals(assignment.getAllowLateSubmission())) {
                throw new LateSubmissionNotAllowedException("Không được nộp bài tập này muộn");
            } else {
                isLate = true;
            }
        }

        // create submission
        Submission sub = new Submission();
        sub.setSubmittedAt(now);
        sub.setAssignment(assignment);
        sub.setStudent(student);
        // set attempt number = existing + 1
        sub.setAttemptNumber(existingCount + 1);
        sub.setIsLate(isLate);
        sub.setStatus(isLate ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED);
        sub.setContent(req.getContent());

        // handle files (example: store filenames as comma-separated) - replace with real storage
        if (files != null && !files.isEmpty()) {
            String fileNames = files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining(","));
            sub.setFileUrl(fileNames);
        }

        sub = submissionRepository.save(sub);

        // process answers and auto-grade option questions (existing logic)
        int totalQ = 0;
        int correctCount = 0;
        float totalScore = 0f;

        List<AnswerRequest> answers = req.getAnswers() == null ? Collections.emptyList() : req.getAnswers();

        // --- inside the submitAssignment loop over answers ---
        for (AnswerRequest ar : answers) {
            totalQ++;
            Question q = questionRepository.findById(ar.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi: " + ar.getQuestionId()));

            Answer ans = new Answer();
            ans.setSubmission(sub);
            ans.setQuestion(q);
            ans.setStudentAnswerText(ar.getTextAnswer());
            ans.setAnswerType(q.getQuestionType() != null ? mapQuestionTypeToAnswerType(q.getQuestionType()) : null);

            QuestionOption chosenOpt = null;
            if (ar.getChosenOptionId() != null) {
                chosenOpt = questionOptionRepository.findById(ar.getChosenOptionId()).orElse(null);
                if (chosenOpt != null) {
                    ans.setChosenOption(chosenOpt);
                } else {
                    // optional: log or set feedback that chosen option not found
                }
            }

            boolean isCorrect = false;
            float qScore = 0f;

            // Auto-grading for multiple-choice / true-false
            QuestionType qt = q.getQuestionType();
            if (qt == QuestionType.MULTIPLE_CHOICE || qt == QuestionType.TRUE_FALSE) {
                if (chosenOpt != null) {
                    // ensure chosen option belongs to the question to avoid mismatched ids
                    if (chosenOpt.getQuestion() != null && Objects.equals(chosenOpt.getQuestion().getQuestionId(), q.getQuestionId())) {
                        if (Boolean.TRUE.equals(chosenOpt.getIsCorrect())) {
                            isCorrect = true;
                            qScore = q.getPoints() != null ? q.getPoints() : 0f;
                        }
                    } else {
                        // mismatch: chosen option doesn't belong to this question
                        // you can mark as incorrect and set feedback
                        ans.setFeedback("Đáp án đã chọn không thuộc về câu hỏi");
                    }
                } else {
                    // no option chosen
                    ans.setFeedback("Không có đáp án nào được chọn");
                }
            } else if (qt == QuestionType.SHORT_ANSWER) {
                // Optional: if you have an answer key in Question (e.g. q.getCorrectAnswerText())
                // implement simple string comparison (case-insensitive) if you store key
                // Example:
                // if (q.getCorrectAnswerText()!=null && ar.getTextAnswer()!=null &&
                //     q.getCorrectAnswerText().trim().equalsIgnoreCase(ar.getTextAnswer().trim())) { ... }
            } else {
                // other types (ESSAY, MATCHING) -> leave for manual grading or AI
            }

            // set fields on answer
            ans.setIsCorrect(isCorrect);
            ans.setScore(qScore);
            if (ans.getFeedback() == null) {
                ans.setFeedback(isCorrect ? "Correct" : "Pending manual review");
            }

            // persist answer
            ans = answerRepository.save(ans);

            if (isCorrect) correctCount++;
            totalScore += qScore;
        }



        // ----- APPLY LATE PENALTY IF NEEDED -----
//        if (Boolean.TRUE.equals(sub.getIsLate()) && assignment.getLatePenalty() != null) {
//            Float latePenalty = assignment.getLatePenalty();
//            // If penalty <= 1 => treat as percentage (0.1 = 10% penalty)
//            if (latePenalty <= 1.0f) {
//                totalScore = totalScore * (1.0f - latePenalty);
//            } else {
//                // If penalty > 1 treat as absolute deduction points
//                totalScore = Math.max(0f, totalScore - latePenalty);
//            }
//        }

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
//        result.setAiFeedback(grade.getAutoGradedFeedback());

        return result;
    }

    private AnswerType mapQuestionTypeToAnswerType(QuestionType qt) {
        if (qt == null) return null;
        switch (qt) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                return AnswerType.OPTION;
            case SHORT_ANSWER:
            case ESSAY:
                return AnswerType.TEXT;
            default:
                return AnswerType.TEXT;
        }
    }


    /* 4) Get submission detail */
    public SubmissionDetailDTO getSubmissionDetail(Long submissionId) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp: " + submissionId));

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
        if (total == 0) return "Không có câu hỏi nào để chấm điểm.";
        float pct = 100f * correctCount / total;
        return String.format("Bạn trả lời đúng %d/%d (%.1f%%). Xem phần giải thích để cải thiện.", correctCount, total, pct);
    }
}
