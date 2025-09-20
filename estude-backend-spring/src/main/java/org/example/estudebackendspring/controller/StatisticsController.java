package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.ClassStatisticsDTO;
import org.example.estudebackendspring.dto.StudentStatisticsDTO;
import org.example.estudebackendspring.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;


    // Student tự xem thống kê của mình
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentStatisticsDTO> getStudentStatistics(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(statisticsService.getStudentStatistics(studentId));
    }

    // Teacher xem thống kê của lớp (hoặc 1 học sinh trong lớp)
    @GetMapping("/teacher/{teacherId}/class/{classId}")
    public ResponseEntity<ClassStatisticsDTO> getClassStatistics(
            @PathVariable Long teacherId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(statisticsService.getClassStatistics(teacherId, classId));
    }

}

