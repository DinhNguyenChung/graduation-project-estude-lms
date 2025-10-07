package org.example.estudebackendspring.controller;


import org.example.estudebackendspring.dto.ApiResponse;
import org.example.estudebackendspring.dto.AssignmentDetailDTO;
import org.example.estudebackendspring.dto.AuthResponse;
import org.example.estudebackendspring.dto.CreateNotificationRequest;
import org.example.estudebackendspring.entity.Assignment;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.entity.Term;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.example.estudebackendspring.enums.NotificationType;
import org.example.estudebackendspring.repository.AssignmentRepository;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.service.AssignmentService;
import org.example.estudebackendspring.service.AssignmentSubmissionService;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionService assignmentSubmissionService;
    private final TeacherRepository teacherRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LogEntryService logEntryService;
    private final NotificationService notificationService;

    public AssignmentController(AssignmentService assignmentService , AssignmentRepository assignmentRepository,
                                AssignmentSubmissionService assignmentSubmissionService, TeacherRepository teacherRepository, ClassSubjectRepository classSubjectRepository,
                                SimpMessagingTemplate messagingTemplate, LogEntryService logEntryService,
                                NotificationService notificationService) {
        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionService = assignmentSubmissionService;
        this.teacherRepository = teacherRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.messagingTemplate = messagingTemplate;
        this.logEntryService = logEntryService;
        this.notificationService = notificationService;
    }
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
    @GetMapping
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }
    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment) {
        try {
            // Lấy teacher từ DB
            Teacher teacher = teacherRepository.findById(assignment.getTeacher().getUserId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            // Lấy classSubject từ DB
            ClassSubject classSubject = classSubjectRepository.findById(assignment.getClassSubject().getClassSubjectId())
                    .orElseThrow(() -> new RuntimeException("ClassSubject not found"));
            // Kiểm tra thời gian hiện tại có nằm trong Term không
//            Term term = classSubject.getTerm();
//            LocalDate today = LocalDate.now();
//            LocalDate begin = convertToLocalDate(term.getBeginDate());
//            LocalDate end = convertToLocalDate(term.getEndDate());
//            if (today.isBefore(begin) || today.isAfter(end)) {
//                Map<String, Object> data = new HashMap<>();
//                data.put("today", today);
//                data.put("termBegin", begin);
//                data.put("termEnd", end);
//
//                return ResponseEntity.badRequest().body(
//                        new AuthResponse(false,
//                                "Không thể tạo bài vì thời gian hiện tại không nằm trong kỳ học [" + begin + " - " + end + "]",
//                                data)
//                );
//            }

            // Gắn teacher và classSubject vào assignment
            assignment.setTeacher(teacher);
            assignment.setClassSubject(classSubject);

            // Lưu assignment
            Assignment created = assignmentService.createAssignment(assignment);
            // Tạo logEntry
            logEntryService.createLog(
                    "Assignment",
                    created.getAssignmentId(),
                    "Tạo mới bài: " + assignment.getTitle() + " của lớp "+created.getClassSubject().getTerm().getClazz().getName()+" và môn học "+ created.getClassSubject().getSubject().getName(),
                    ActionType.CREATE,
                    created.getClassSubject().getClassSubjectId(),
                    "ClassSubject",
                    created.getTeacher()
            );
            // Tạo Notification cho bài tập
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
            String formattedDueDate = created.getDueDate().format(formatter);

            String message = assignment.getTitle()  + " môn "+created.getClassSubject().getSubject().getName()+" với thời gian hoàn thành " +formattedDueDate;
            // Tạo request
            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest();
            createNotificationRequest.setMessage(message);
            createNotificationRequest.setPriority(NotificationPriority.MEDIUM);
            createNotificationRequest.setTargetType(NotificationTargetType.CLASS_SUBJECT);
            createNotificationRequest.setTargetId(created.getClassSubject().getClassSubjectId());
            createNotificationRequest.setType(NotificationType.ASSIGNMENT_REMINDER);

            // Gọi notificationService
            notificationService.createNotification(createNotificationRequest,created.getTeacher());

            // Gửi thông báo WebSocket cho FE (các client subscribe)
            messagingTemplate.convertAndSend(
                    "/topic/class/" + classSubject.getClassSubjectId() + "/assignments",
                    created
            );
            return ResponseEntity.ok(
                    new AuthResponse(true, "Assignment created successfully", created)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, e.getMessage(), null));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Unexpected error", null));
        }
    }


    // GET /api/assignments/{assignmentId}
    @GetMapping("/{assignmentId}")
    public ResponseEntity<?> getAssignment(@PathVariable Long assignmentId) {
        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            return ResponseEntity.ok(
                    new AuthResponse(true, "Assignment found", assignment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                    new AuthResponse(false, "Assignment not found", null)
            );
        }
    }

    // PUT /api/assignments/{assignmentId}
    @PutMapping("/{assignmentId}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody Assignment updated) {
        try {
            Assignment assignment = assignmentService.updateAssignment(assignmentId, updated);
            // tạo log
            logEntryService.createLog(
                    "Assignment",
                    assignment.getAssignmentId(),
                    "Cập nhật bài: " + assignment.getTitle() + " của lớp "+assignment.getClassSubject().getTerm().getClazz().getName()+" và môn học "+ assignment.getClassSubject().getSubject().getName(),
                    ActionType.UPDATE,
                    assignment.getClassSubject().getClassSubjectId(),
                    "ClassSubject",
                    assignment.getTeacher()
            );
            // Tạo Notification cho bài tập
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
//            String formattedDueDate = assignment.getDueDate().format(formatter);
//            String message = assignment.getTitle()+" môn "+assignment.getClassSubject().getSubject().getName()+ " được cập nhật lại với thời gian hoàn thành " +formattedDueDate;
//
//            // Tạo request
//            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest();
//            createNotificationRequest.setMessage(message);
//            createNotificationRequest.setPriority(NotificationPriority.MEDIUM);
//            createNotificationRequest.setTargetType(NotificationTargetType.CLASS_SUBJECT);
//            createNotificationRequest.setTargetId(assignment.getClassSubject().getClassSubjectId());
//            createNotificationRequest.setType(NotificationType.ASSIGNMENT_REMINDER);

            // Gọi notificationService
//            notificationService.createNotification(createNotificationRequest,assignment.getTeacher());
            //  Gửi thông báo cập nhật
            messagingTemplate.convertAndSend(
                    "/topic/class/" + assignment.getClassSubject().getClassSubjectId() + "/assignments",
                    assignment
            );
            return ResponseEntity.ok(
                    new AuthResponse(true, "Assignment updated successfully", assignment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                    new AuthResponse(false, "Failed to update assignment", null)
            );
        }
    }

    // DELETE /api/assignments/{assignmentId}
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            assignmentService.deleteAssignment(assignmentId);
            // Tạo log
            logEntryService.createLog(
                    "Assignment",
                    assignmentId,
                    "Xóa bài tập: " + assignment.getTitle() + " của lớp "+assignment.getClassSubject().getTerm().getClazz().getName()+" và môn học "+ assignment.getClassSubject().getSubject().getName(),
                    ActionType.DELETE,
                    assignment.getClassSubject().getClassSubjectId(),
                    "ClassSubject",
                    assignment.getTeacher()
            );
            // Tạo Notification cho xóa bài tập
//            String message = assignment.getTitle()+" môn "+assignment.getClassSubject().getSubject().getName()+ " đã được xóa";
//
//            // Tạo request
//            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest();
//            createNotificationRequest.setMessage(message);
//            createNotificationRequest.setPriority(NotificationPriority.MEDIUM);
//            createNotificationRequest.setTargetType(NotificationTargetType.CLASS_SUBJECT);
//            createNotificationRequest.setTargetId(assignment.getClassSubject().getClassSubjectId());
//            createNotificationRequest.setType(NotificationType.ASSIGNMENT_REMINDER);
//
//            // Gọi notificationService
//            notificationService.createNotification(createNotificationRequest,assignment.getTeacher());
            messagingTemplate.convertAndSend(
                    "/topic/class/" + assignment.getClassSubject().getClassSubjectId() + "/assignments",
                    "Assignment with ID " + assignmentId + " deleted"
            );
            return ResponseEntity.ok(
                    new ApiResponse(true, "Assignment deleted successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                    new ApiResponse(false, "Failed to delete assignment")
            );
        }
    }
    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getAssignmentsByClass(@PathVariable Long classId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByClass(classId);
        if (assignments.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("No assignments found for classId: " + classId);
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(assignments);
    }
    @GetMapping("/class-subject/{classSubjectId}")
    public ResponseEntity<?> getAssignmentsByClassSubject(@PathVariable Long classSubjectId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByClassSubject(classSubjectId);
        if (assignments.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No assignments found for classId: " + classSubjectId);
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(assignments);
    }
}