package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.CreateNotificationRequest;
import org.example.estudebackendspring.dto.ScheduleDTO;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Schedule;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.ActionType;
import org.example.estudebackendspring.enums.NotificationPriority;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.example.estudebackendspring.enums.NotificationType;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.LogEntryService;
import org.example.estudebackendspring.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody Schedule schedule) {
        ScheduleDTO createdSchedule = scheduleService.createSchedule(schedule);
        
        // Log schedule creation
        try {
            String description = "Tạo thời khóa biểu mới";
            if (createdSchedule.getStartPeriod() != null && createdSchedule.getEndPeriod() != null) {
                description += " (" + createdSchedule.getStartPeriod() + " - " + createdSchedule.getEndPeriod() + ")";
            }
            User user = userRepository.findById(createdSchedule.getClassSubject().getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
            logEntryService.createLog(
                "Schedule",
                createdSchedule.getScheduleId(),
                description,
                ActionType.CREATE,
                createdSchedule.getClassSubject().getClassSubjectId(),
                    "ClassSubject",
                    user

            );
//            // Tạo Notification cho lịch học
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
//
//            String message = "Có lịch học mới trong tuần: " + createdSchedule.getWeek()
//                    + " môn " + session.getSubjectName()
//                    + " (" + session.getStartTime().format(formatter) + " - " + session.getEndTime().format(formatter) + ")";
//            // Tạo request
//            CreateNotificationRequest createNotificationRequest = new CreateNotificationRequest();
//            createNotificationRequest.setMessage(message);
//            createNotificationRequest.setPriority(NotificationPriority.MEDIUM);
//            createNotificationRequest.setTargetType(NotificationTargetType.CLASS_SUBJECT);
//            createNotificationRequest.setTargetId(session.getClassSubjectId());
//            createNotificationRequest.setType(NotificationType.ATTENDANCE_REMINDER);
//
//            // Gọi notificationService
//            notificationService.createNotification(createNotificationRequest,teacher);    gi
        } catch (Exception e) {
            System.err.println("Failed to log schedule creation: " + e.getMessage());
        }

        // Gửi WebSocket đến tất cả client đang theo dõi lớp đó
        messagingTemplate.convertAndSend(
                "/topic/class/" + createdSchedule.getClassSubject().getClassSubjectId() + "/schedules",
                createdSchedule
        );
        
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Long scheduleId, @RequestBody Schedule schedule) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateSchedule(scheduleId, schedule);
            
            // Log schedule update
            try {
                User user = userRepository.findById(updatedSchedule.getClassSubject().getTeacherId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid actingUserId"));
                String description = "Cập nhật thời khóa biểu";
                if (updatedSchedule.getStartPeriod() != null && updatedSchedule.getEndPeriod() != null) {
                    description += " (" + updatedSchedule.getStartPeriod() + " - " + updatedSchedule.getEndPeriod() + ")";
                }
                logEntryService.createLog(
                    "Schedule",
                    scheduleId,
                    description,
                    ActionType.UPDATE,
                    updatedSchedule.getClassSubject().getClassSubjectId(),
                    "ClassSubject",
                    user
                );
            } catch (Exception e) {
                System.err.println("Failed to log schedule update: " + e.getMessage());
            }
            // Gửi socket
            messagingTemplate.convertAndSend(
                    "/topic/class/" +  updatedSchedule.getClassSubject().getClassSubjectId() + "/schedules",
                    updatedSchedule
            );
            return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            // Log schedule deletion
            Schedule deleted = scheduleService.getScheduleById(scheduleId);
            ClassSubject classSubject = deleted.getClassSubject();
            try {
                logEntryService.createLog(
                    "Schedule",
                    scheduleId,
                    "Xóa thời khóa biểu",
                    ActionType.DELETE,
                    null,
                    "",
                    null
                );
            } catch (Exception e) {
                System.err.println("Failed to log schedule deletion: " + e.getMessage());
            }
            
            scheduleService.deleteSchedule(scheduleId);
            // Gửi socket
            messagingTemplate.convertAndSend(
                    "/topic/class/" + classSubject.getClassSubjectId() + "/schedules",
                    Map.of("action", "DELETE", "scheduleId", scheduleId)
            );

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/teacher/{teacherId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<ScheduleDTO>> getSchedulesForTeacher(@PathVariable Long teacherId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesForTeacher(teacherId);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesForStudent(@PathVariable Long studentId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesForStudent(studentId);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByClassId(@PathVariable Long classId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByClassId(classId);
        return ResponseEntity.ok(schedules);
    }
}
