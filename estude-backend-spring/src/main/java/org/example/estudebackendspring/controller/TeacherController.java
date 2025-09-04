package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.StudentDTO;
import org.example.estudebackendspring.dto.SubjectDTO;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.example.estudebackendspring.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final ClassSubjectService classSubjectService;
    private final TeacherRepository teacherRepository;

    public TeacherController(TeacherService teacherService, ClassSubjectService classSubjectService, TeacherRepository teacherRepository) {
        this.teacherService = teacherService;
        this.classSubjectService = classSubjectService;
        this.teacherRepository = teacherRepository;
    }
    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }
    @GetMapping("/{teacherId}")
    public ResponseEntity<?> getTeacherById(@PathVariable Long teacherId) {
        return teacherService.getTeacherById(teacherId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{teacherId}/homeroom-students")
    public ResponseEntity<List<Student>> getHomeroomStudents(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getHomeroomStudents(teacherId));
    }

//    @GetMapping("/{teacherId}/subjects/{subjectId}/students")
//    public ResponseEntity<List<Student>> getStudentsBySubject(
//            @PathVariable Long teacherId,
//            @PathVariable Long subjectId) {
//        return ResponseEntity.ok(teacherService.getStudentsBySubject(teacherId, subjectId));
//    }
    @GetMapping("/{teacherId}/class-subjects")
    public ResponseEntity<List<ClassSubject>> getClassSubjectsByTeacher(@PathVariable Long teacherId) {
        List<ClassSubject> classSubjects = classSubjectService.getClassSubjectsByTeacher(teacherId);
        return ResponseEntity.ok(classSubjects);
    }
    @GetMapping("/{teacherId}/subjects")
    public ResponseEntity<List<SubjectDTO>> getSubjectsByTeacher(@PathVariable Long teacherId) {
        List<SubjectDTO> subjects = teacherService.getSubjectsByTeacher(teacherId);
        return ResponseEntity.ok(subjects);
    }
    @GetMapping("/{teacherId}/subjects/{subjectId}/students")
    public ResponseEntity<List<StudentDTO>> getStudentsByTeacherAndSubject(
            @PathVariable Long teacherId,
            @PathVariable Long subjectId) {
        List<StudentDTO> students = teacherService.getStudentsByTeacherAndSubject(teacherId, subjectId);
        return ResponseEntity.ok(students);
    }

}