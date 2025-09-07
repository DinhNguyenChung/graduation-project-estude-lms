package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.enums.AttendanceMethod;
import org.example.estudebackendspring.enums.AttendanceStatus;
import org.example.estudebackendspring.exception.BadRequestException;
import org.example.estudebackendspring.exception.ForbiddenException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.exception.UnauthorizedException;
import org.example.estudebackendspring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private final ClassSubjectRepository classSubjectRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private EnrollmentRepository enrollmentRepository;

    public AttendanceService(ClassSubjectRepository classSubjectRepository, AttendanceSessionRepository sessionRepository, EnrollmentRepository enrollmentRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.classSubjectRepository = classSubjectRepository;
        this.sessionRepository = sessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }
    // Hằng số cho khoảng cách GPS tối đa (mét)
    private static final double MAX_ALLOWED_DISTANCE = 50.0;

    // Giáo viên tạo buổi điểm danh
    @Transactional
    public AttendanceSessionDTO createAttendanceSession(Long teacherId, Long classSubjectId, String sessionName, LocalDateTime startTime, LocalDateTime endTime, Double gpsLatitude, Double gpsLongitude) {
        ClassSubject classSubject = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new IllegalArgumentException("ClassSubject not found"));

        // Kiểm tra giáo viên có quyền tạo điểm danh cho môn học này
        if (!classSubject.getTeacher().getUserId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher is not authorized for this ClassSubject");
        }

        AttendanceSession session = new AttendanceSession();
        session.setTeacher(new Teacher(teacherId));
        session.setClassSubject(classSubject);
        session.setSessionName(sessionName);
        session.setCreateAt(LocalDateTime.now());
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setGpsLatitude(gpsLatitude);
        session.setGpsLongitude(gpsLongitude);

        AttendanceSession savedSession = sessionRepository.save(session);
        return toAttendanceSessionDTO(savedSession,null);
    }

    // Giáo viên xem danh sách học sinh đã điểm danh
    public List<AttendanceRecordDTO> getAttendanceRecordsBySession(Long sessionId, Long teacherId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("AttendanceSession not found"));

        // Kiểm tra quyền của giáo viên
        if (!session.getTeacher().getUserId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher is not authorized for this session");
        }

        return attendanceRecordRepository.findBySessionSessionId(sessionId)
                .stream()
                .map(this::toAttendanceRecordDTO)
                .collect(Collectors.toList());
    }

    // Giáo viên điểm danh giúp học sinh
    @Transactional
    public AttendanceRecordDTO markAttendanceByTeacher(Long sessionId, Long studentId, Long teacherId, AttendanceStatus status) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("AttendanceSession not found"));

        // Kiểm tra quyền của giáo viên
        if (!session.getTeacher().getUserId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher is not authorized for this session");
        }

        // Kiểm tra học sinh có trong lớp của ClassSubject
        boolean isEnrolled = enrollmentRepository.findByClazzClassId(session.getClassSubject().getClazz().getClassId())
                .stream().anyMatch(e -> e.getStudent().getUserId().equals(studentId));
        if (!isEnrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this Class");
        }

        // Kiểm tra xem đã có bản ghi điểm danh chưa
        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository.findBySessionSessionIdAndStudent_UserId(sessionId, studentId);
        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            // Cập nhật bản ghi hiện có
            record = existingRecord.get();
            record.setStatus(status);
            record.setTimestamp(LocalDateTime.now());
//            record.setMethod(AttendanceMethod.BUTTON_PRESS);
        } else {
            // Tạo bản ghi mới
            record = new AttendanceRecord();
            record.setSession(session);
            record.setStudent(new Student(studentId));
            record.setStatus(status);
            record.setTimestamp(LocalDateTime.now());
            record.setMethod(AttendanceMethod.BUTTON_PRESS);
        }

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        return toAttendanceRecordDTO(savedRecord);
    }

    // Học sinh xem danh sách buổi điểm danh của lớp môn học
    public List<AttendanceSessionDTO> getAttendanceSessionsByClassSubject(Long classSubjectId, Long studentId) {
        ClassSubject classSubject = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp môn học này"));

        // Kiểm tra học sinh có trong lớp của ClassSubject
        boolean isEnrolled = enrollmentRepository.findByClazzClassId(classSubject.getClazz().getClassId())
                .stream().anyMatch(e -> e.getStudent().getUserId().equals(studentId));
        if (!isEnrolled) {
            throw new IllegalArgumentException("Học sinh không nằm trong lớp học này");
        }

        return sessionRepository.findByClassSubjectClassSubjectId(classSubjectId)
                .stream()
                .map(session -> toAttendanceSessionDTO(session, studentId))
                .collect(Collectors.toList());
    }

    // Học sinh thực hiện điểm danh
    @Transactional
    public AttendanceRecordDTO markAttendanceByStudent(Long sessionId, Long studentId,AttendanceMethod method, Double gpsLatitude, Double gpsLongitude) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy buổi điểm danh này"));

        // Kiểm tra thời gian điểm danh
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new IllegalArgumentException("Không được phép điểm danh ngoài thời gian cho phép");
        }

        // Kiểm tra học sinh có trong lớp của ClassSubject
        boolean isEnrolled = enrollmentRepository.findByClazzClassId(session.getClassSubject().getClazz().getClassId())
                .stream().anyMatch(e -> e.getStudent().getUserId().equals(studentId));
        if (!isEnrolled) {
            throw new IllegalArgumentException("Học sinh không nằm trong lớp học này");
        }

        // Kiểm tra xem đã có bản ghi điểm danh chưa
        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository.findBySessionSessionIdAndStudent_UserId(sessionId, studentId);
        if (existingRecord.isPresent()) {
            throw new IllegalArgumentException("Hồ sơ điểm danh đã tồn tại cho học sinh này trong phiên này");
        }

        // Kiểm tra GPS (chỉ thực hiện nếu cả hai tọa độ của session đều không null)
        if (session.getGpsLatitude() != null && session.getGpsLongitude() != null) {
            if (gpsLatitude == null || gpsLongitude == null) {
                throw new IllegalArgumentException("Tọa độ GPS của học sinh không được để trống khi buổi điểm danh yêu cầu GPS");
            }
            double distance = calculateHaversineDistance(session.getGpsLatitude(), session.getGpsLongitude(), gpsLatitude, gpsLongitude);
            if (distance > MAX_ALLOWED_DISTANCE) {
                throw new IllegalArgumentException("Học viên ở quá xa địa điểm học");
            }
        }


        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(new Student(studentId));
//        record.setStatus(AttendanceStatus.PRESENT);
        long minutesLate = Duration.between(session.getStartTime(), now).toMinutes();
        if (minutesLate > 15) { // Muộn quá 15 phút
            record.setStatus(AttendanceStatus.LATE);
        } else {
            record.setStatus(AttendanceStatus.PRESENT);
        }
        record.setTimestamp(now);
        record.setGpsLatitude(gpsLatitude);
        record.setGpsLongitude(gpsLongitude);
        record.setMethod(method);

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        return toAttendanceRecordDTO(savedRecord);
    }
    // Giáo viên xem danh sách học sinh và trạng thái điểm danh trong một buổi điểm danh
    public List<StudentAttendanceDTO> getStudentAttendanceList(Long sessionId, Long teacherId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("AttendanceSession not found"));

        // Kiểm tra quyền của giáo viên
        if (!session.getTeacher().getUserId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher is not authorized for this session");
        }

        // Lấy danh sách học sinh từ Enrollment của Clazz
        Long classId = session.getClassSubject().getClazz().getClassId();
        List<Enrollment> enrollments = enrollmentRepository.findByClazzClassId(classId);

        // Lấy danh sách bản ghi điểm danh trong session
        List<AttendanceRecord> records = attendanceRecordRepository.findBySessionSessionId(sessionId);

        // Chuyển đổi thành StudentAttendanceDTO
        return enrollments.stream().map(enrollment -> {
            Student student = enrollment.getStudent();
            StudentAttendanceDTO dto = new StudentAttendanceDTO();
            dto.setStudentId(student.getUserId());
            dto.setStudentCode(student.getStudentCode());
            dto.setStudentName(student.getFullName()); // Giả định Student/User có trường name

            // Kiểm tra trạng thái điểm danh
            Optional<AttendanceRecord> record = records.stream()
                    .filter(r -> r.getStudent().getUserId().equals(student.getUserId()))
                    .findFirst();
            dto.setStatus(record.map(AttendanceRecord::getStatus).orElse(null));

            return dto;
        }).collect(Collectors.toList());
    }
    // Giáo viên xem danh sách buổi điểm danh theo ClassSubject
    public List<AttendanceSessionDTO> getAttendanceSessionsByClassSubjectForTeacher(Long classSubjectId, Long teacherId) {
        ClassSubject classSubject = classSubjectRepository.findById(classSubjectId)
                .orElseThrow(() -> new IllegalArgumentException("ClassSubject not found"));

        // Kiểm tra quyền của giáo viên
        if (!classSubject.getTeacher().getUserId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher is not authorized for this ClassSubject");
        }

        return sessionRepository.findByClassSubjectClassSubjectId(classSubjectId)
                .stream()
                .map(session -> toAttendanceSessionDTO(session, null))
                .collect(Collectors.toList());
    }
    // Chuyển đổi AttendanceSession thành DTO
    private AttendanceSessionDTO toAttendanceSessionDTO(AttendanceSession session, Long studentId) {
        AttendanceSessionDTO dto = new AttendanceSessionDTO();
        dto.setSessionId(session.getSessionId());
        dto.setTeacherId(session.getTeacher().getUserId());
        dto.setTeacherCode(session.getTeacher().getTeacherCode());
        dto.setClassSubjectId(session.getClassSubject().getClassSubjectId());
        dto.setSessionName(session.getSessionName());
        dto.setCreateAt(session.getCreateAt());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setGpsLatitude(session.getGpsLatitude());
        dto.setGpsLongitude(session.getGpsLongitude());
        // Lấy trạng thái điểm danh của học sinh cụ thể
        if (studentId != null) {
            Optional<AttendanceRecord> record = attendanceRecordRepository.findBySessionSessionIdAndStudent_UserId(session.getSessionId(), studentId);
            dto.setStatus(record.map(AttendanceRecord::getStatus).orElse(null));
        }

        return dto;
    }

    // Chuyển đổi AttendanceRecord thành DTO
    private AttendanceRecordDTO toAttendanceRecordDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setAttendanceId(record.getAttendanceId());
        dto.setSessionId(record.getSession().getSessionId());
        dto.setStudentId(record.getStudent().getUserId());
        dto.setStudentCode(record.getStudent().getStudentCode());
        dto.setMethod(record.getMethod());
        dto.setGpsLatitude(record.getGpsLatitude());
        dto.setGpsLongitude(record.getGpsLongitude());
        dto.setStatus(record.getStatus());
        dto.setTimestamp(record.getTimestamp());
        return dto;
    }

    // Hàm tính khoảng cách Haversine
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Bán kính Trái Đất (mét)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

