package org.example.estudebackendspring.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.AttendanceRecord;
import org.example.estudebackendspring.entity.AttendanceSession;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.enums.*;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.service.AttendanceService;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LogEntryService logEntryService;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public AttendanceController(AttendanceService attendanceService, SimpMessagingTemplate messagingTemplate,
                               LogEntryService logEntryService, TeacherRepository teacherRepository, 
                               StudentRepository studentRepository, NotificationService notificationService) {
        this.attendanceService = attendanceService;
        this.messagingTemplate = messagingTemplate;
        this.logEntryService = logEntryService;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }
    // Giáo viên tạo buổi điểm danh
    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionDTO> createAttendanceSession(
            @RequestParam Long teacherId,
            @RequestParam Long classSubjectId,
            @RequestParam String sessionName,
            @RequestParam String startTime, // Format: "2025-09-07T10:00:00"
            @RequestParam String endTime,   // Format: "2025-09-07T12:00:00"
            @RequestParam(required = false) Double gpsLatitude,
            @RequestParam(required = false) Double gpsLongitude) {
        AttendanceSessionDTO session = attendanceService.createAttendanceSession(
                teacherId,
                classSubjectId,
                sessionName,
                LocalDateTime.parse(startTime),
                LocalDateTime.parse(endTime),
                gpsLatitude,
                gpsLongitude);
        
        // Tạo log entry
        try {
            Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
            logEntryService.createLog(
                    "AttendanceSession",
                    session.getSessionId(),
                    "Tạo buổi điểm danh: " + sessionName + " từ " + startTime + " đến " + endTime,
                    ActionType.CREATE,
                    classSubjectId,
                    "ClassSubject",
                    teacher
            );
            // Tạo Notification cho bài tập
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

            String message = "Có buổi điểm danh mới: " + session.getSessionName()
                    + " môn " + session.getSubjectName()
                    + " (" + session.getStartTime().format(formatter) + " - " + session.getEndTime().format(formatter) + ")";
            // Tạo request
            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest();
            createNotificationRequest.setMessage(message);
            createNotificationRequest.setPriority(NotificationPriority.MEDIUM);
            createNotificationRequest.setTargetType(NotificationTargetType.CLASS_SUBJECT);
            createNotificationRequest.setTargetId(session.getClassSubjectId());
            createNotificationRequest.setType(NotificationType.ATTENDANCE_REMINDER);

            // Gọi notificationService
            notificationService.createNotification(createNotificationRequest,teacher);

        } catch (Exception e) {
            log.warn("Failed to log attendance session creation", e);
        }
        
        // thông báo cho tất cả học sinh trong lớp
        messagingTemplate.convertAndSend(
                "/topic/class/" + classSubjectId + "/sessions",
                session
        );
        return ResponseEntity.ok(session);
    }
    // Update buổi điểm danh
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<?> updateAttendanceSession(
            @PathVariable Long sessionId,
            @RequestParam Long teacherId,
            @RequestBody UpdateAttendanceSessionRequest request
    ) {
        try {
            AttendanceSessionDTO updated = attendanceService.updateAttendanceSession(
                    sessionId, teacherId, request);

            // log
            Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
            logEntryService.createLog(
                    "AttendanceSession",
                    sessionId,
                    "Cập nhật buổi điểm danh: " + request.getSessionName(),
                    ActionType.UPDATE,
                    updated.getClassSubjectId(),
                    "ClassSubject",
                    teacher
            );

            // notification (ví dụ chỉ update message gửi cho học sinh)
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
//            String message = "Cập nhật buổi điểm danh: " + updated.getSessionName()
//                    + " môn " + updated.getSubjectName()
//                    + " (" + updated.getStartTime().format(formatter) + " - " + updated.getEndTime().format(formatter) + ")";
//            CreateNotificationRequest notifReq = new CreateNotificationRequest();
//            notifReq.setMessage(message);
//            notifReq.setPriority(NotificationPriority.MEDIUM);
//            notifReq.setTargetType(NotificationTargetType.CLASS_SUBJECT);
//            notifReq.setTargetId(updated.getClassSubjectId());
//            notifReq.setType(NotificationType.ATTENDANCE_REMINDER);
//
//            notificationService.createNotification(notifReq, teacher);
            // thông báo cho tất cả học sinh trong lớp
            messagingTemplate.convertAndSend(
                    "/topic/class/" + updated.getClassSubjectId() + "/sessions",
                    updated
            );
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete buổi điểm danh
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteAttendanceSession(
            @PathVariable Long sessionId,
            @RequestParam Long teacherId
    ) {
        try {
            AttendanceSessionDTO deleted = attendanceService.deleteAttendanceSession(sessionId, teacherId);

            Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
            logEntryService.createLog(
                    "AttendanceSession",
                    sessionId,
                    "Xóa buổi điểm danh: " + deleted.getSessionName(),
                    ActionType.DELETE,
                    deleted.getClassSubjectId(),
                    "ClassSubject",
                    teacher
            );
            // notification (ví dụ chỉ update message gửi cho học sinh)
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
//
//            String message = "Đã xóa buổi điểm danh: " + deleted.getSessionName()
//                    + " môn " + deleted.getSubjectName()
//                    + " (" + deleted.getStartTime().format(formatter) + " - " + deleted.getEndTime().format(formatter) + ")";
//            CreateNotificationRequest notifReq = new CreateNotificationRequest();
//            notifReq.setMessage(message);
//            notifReq.setPriority(NotificationPriority.MEDIUM);
//            notifReq.setTargetType(NotificationTargetType.CLASS_SUBJECT);
//            notifReq.setTargetId(deleted.getClassSubjectId());
//            notifReq.setType(NotificationType.ATTENDANCE_REMINDER);
//
//            notificationService.createNotification(notifReq, teacher);
            // thông báo cho tất cả học sinh trong lớp
            messagingTemplate.convertAndSend(
                    "/topic/class/" + deleted.getClassSubjectId() + "/sessions",
                    deleted
            );
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Giáo viên xem danh sách học sinh đã điểm danh
    @GetMapping("/sessions/{sessionId}/records")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceRecords(
            @PathVariable Long sessionId,
            @RequestParam Long teacherId) {
        List<AttendanceRecordDTO> records = attendanceService.getAttendanceRecordsBySession(sessionId, teacherId);
        return ResponseEntity.ok(records);
    }

    // Giáo viên điểm danh giúp học sinh
    @PostMapping("/records/teacher")
    public ResponseEntity<AttendanceRecordDTO> markAttendanceByTeacher(
            @RequestParam Long sessionId,
            @RequestParam Long studentId,
            @RequestParam Long teacherId,
            @RequestParam AttendanceStatus status) {
        AttendanceRecordDTO record = attendanceService.markAttendanceByTeacher(sessionId, studentId, teacherId, status);
        
        // Tạo log entry
        try {
            Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
            Student student = studentRepository.findById(studentId).orElse(null);
            String studentName = student != null ? student.getFullName() : "Unknown Student";
            
            logEntryService.createLog(
                    "AttendanceRecord",
                    record.getAttendanceId(),
                    "Giáo viên điểm danh cho học sinh " + studentName + " với trạng thái: " + status.name(),
                    ActionType.ATTENDANCE,
                    sessionId,
                    "AttendanceSession",
                    teacher
            );
        } catch (Exception e) {
            log.warn("Failed to log teacher attendance marking", e);
        }
        
        // 🔔 thông báo cho học sinh và các client khác
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/records",
                record
        );
        return ResponseEntity.ok(record);
    }

    // Học sinh xem danh sách buổi điểm danh
    @GetMapping("/sessions/class-subject/{classSubjectId}")
    public ResponseEntity<List<AttendanceSessionDTO>> getAttendanceSessions(
            @PathVariable Long classSubjectId,
            @RequestParam Long studentId) {
        List<AttendanceSessionDTO> sessions = attendanceService.getAttendanceSessionsByClassSubject(classSubjectId, studentId);
        return ResponseEntity.ok(sessions);
    }

    // Học sinh thực hiện điểm danh
    @PostMapping("/records/student")
    public ResponseEntity<AttendanceRecordDTO> markAttendanceByStudent(
            @RequestParam Long sessionId,
            @RequestParam Long studentId,
            @RequestParam AttendanceMethod method,
            @RequestParam(required = false) Double gpsLatitude,
            @RequestParam(required = false) Double gpsLongitude) {
        AttendanceRecordDTO record = attendanceService.markAttendanceByStudent(sessionId, studentId,method, gpsLatitude, gpsLongitude);
        
        // Tạo log entry
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            
            logEntryService.createLog(
                    "AttendanceRecord",
                    record.getAttendanceId(),
                    "Học sinh tự điểm danh bằng phương thức: " + method.name() + 
                    (gpsLatitude != null && gpsLongitude != null ? " tại vị trí: " + gpsLatitude + ", " + gpsLongitude : ""),
                    ActionType.ATTENDANCE,
                    sessionId,
                    "AttendanceSession",
                    student
            );
        } catch (Exception e) {
            log.warn("Failed to log student attendance marking", e);
        }
        
        // 🔔 thông báo cho giáo viên quản lý buổi này
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/records",
                record
        );
        return ResponseEntity.ok(record);
    }
    // Giáo viên xem danh sách học sinh và trạng thái điểm danh
    @GetMapping("/sessions/{sessionId}/students")
    public ResponseEntity<List<StudentAttendanceDTO>> getStudentAttendanceList(
            @PathVariable Long sessionId,
            @RequestParam Long teacherId) {
        List<StudentAttendanceDTO> studentList = attendanceService.getStudentAttendanceList(sessionId, teacherId);
        return ResponseEntity.ok(studentList);
    }
    // Giáo viên xem danh sách buổi điểm danh theo ClassSubject
    @GetMapping("/sessions/class-subject/{classSubjectId}/teacher")
    public ResponseEntity<List<AttendanceSessionDTO>> getAttendanceSessionsByClassSubjectForTeacher(
            @PathVariable Long classSubjectId,
            @RequestParam Long teacherId) {
        List<AttendanceSessionDTO> sessions = attendanceService.getAttendanceSessionsByClassSubjectForTeacher(classSubjectId, teacherId);
        return ResponseEntity.ok(sessions);
    }
    // lấy all điểm danh của học sinh
    @GetMapping("/records/student/{studentId}")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceRecordsByStudent(
            @PathVariable Long studentId) {
        List<AttendanceRecordDTO> records = attendanceService.getAttendanceRecordsByStudent(studentId);
        return ResponseEntity.ok(records);
    }

}

