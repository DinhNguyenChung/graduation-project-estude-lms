package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.ScheduleDTO;
import org.example.estudebackendspring.entity.Schedule;
import org.example.estudebackendspring.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody Schedule schedule) {
        ScheduleDTO createdSchedule = scheduleService.createSchedule(schedule);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Long scheduleId, @RequestBody Schedule schedule) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateSchedule(scheduleId, schedule);
            return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesForTeacher(@PathVariable Long teacherId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesForTeacher(teacherId);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesForStudent(@PathVariable Long studentId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesForStudent(studentId);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }
}
