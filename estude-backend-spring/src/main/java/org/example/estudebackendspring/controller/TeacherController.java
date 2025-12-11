package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.service.ClassSubjectService;
import org.example.estudebackendspring.service.TeacherGradeService;
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
    private final TeacherGradeService teacherGradeService;

    public TeacherController(TeacherService teacherService,
                             ClassSubjectService classSubjectService,
                             TeacherRepository teacherRepository
    , TeacherGradeService teacherGradeService) {
        this.teacherService = teacherService;
        this.classSubjectService = classSubjectService;
        this.teacherRepository = teacherRepository;
        this.teacherGradeService = teacherGradeService;
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<ClazzWithStudentsDTO>> getHomeroomStudents(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getHomeroomStudents(teacherId));
    }

//    @GetMapping("/{teacherId}/subjects/{subjectId}/students")
//    public ResponseEntity<List<Student>> getStudentsBySubject(
//            @PathVariable Long teacherId,
//            @PathVariable Long subjectId) {
//        return ResponseEntity.ok(teacherService.getStudentsBySubject(teacherId, subjectId));
//    }
    @GetMapping("/{teacherId}/class-subjects")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getClassSubjectsByTeacher(@PathVariable Long teacherId) {
        List<ClassSubject> classSubjects = classSubjectService.getClassSubjectsByTeacher(teacherId);
        List<ClassSubjectDTO> dtoList = classSubjects.stream()
                .map(cs -> new ClassSubjectDTO(
                        cs.getClassSubjectId(),
                        cs.getSubject() != null ? cs.getSubject().getSubjectId() : null,
                        cs.getSubject() != null ? cs.getSubject().getName() : null,
                        cs.getTeacher() != null ? cs.getTeacher().getFullName() : null,
                        cs.getTerm() != null ? cs.getTerm().getTermId() :null,
                        cs.getTerm() != null ? cs.getTerm().getName() :null,
                        cs.getTerm() != null ? cs.getTerm().getBeginDate() :null,
                        cs.getTerm() != null ? cs.getTerm().getEndDate() :null,
                        cs.getTerm().getClazz()!= null ? cs.getTerm().getClazz().getClassId():null,
                        cs.getTerm().getClazz() != null ? cs.getTerm().getClazz().getName() : null,
                        cs.getTerm().getClazz() != null ? cs.getTerm().getClazz().getGradeLevel() : null

                ))
                .toList();

        return ResponseEntity.ok(dtoList);

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
    // GET /api/teacher/grades/{classSubjectId}
    @GetMapping("/grades/class-subject/{classSubjectId}")
    public ResponseEntity<List<StudentGradeResponse>> getGrades(
            @PathVariable Long classSubjectId
    ) {
        List<StudentGradeResponse> response = teacherGradeService.getGradesByClassSubject(classSubjectId);
        return ResponseEntity.ok(response);
    }
}