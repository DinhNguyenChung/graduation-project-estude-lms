package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.AttendanceRecordDTO;
import org.example.estudebackendspring.dto.AttendanceSessionDTO;
import org.example.estudebackendspring.dto.StudentAttendanceDTO;
import org.example.estudebackendspring.entity.AttendanceRecord;
import org.example.estudebackendspring.entity.AttendanceSession;
import org.example.estudebackendspring.enums.AttendanceMethod;
import org.example.estudebackendspring.enums.AttendanceStatus;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
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

