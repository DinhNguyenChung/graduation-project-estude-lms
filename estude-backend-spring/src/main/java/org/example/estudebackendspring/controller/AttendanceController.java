package org.example.estudebackendspring.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.AttendanceRecordDTO;
import org.example.estudebackendspring.dto.AttendanceSessionDTO;
import org.example.estudebackendspring.dto.StudentAttendanceDTO;
import org.example.estudebackendspring.entity.AttendanceRecord;
import org.example.estudebackendspring.entity.AttendanceSession;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.enums.AttendanceMethod;
import org.example.estudebackendspring.enums.AttendanceStatus;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.service.AttendanceService;
import org.example.estudebackendspring.service.LogEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    public AttendanceController(AttendanceService attendanceService, SimpMessagingTemplate messagingTemplate,
                               LogEntryService logEntryService, TeacherRepository teacherRepository, 
                               StudentRepository studentRepository) {
        this.attendanceService = attendanceService;
        this.messagingTemplate = messagingTemplate;
        this.logEntryService = logEntryService;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
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

