package org.example.estudebackendspring.controller;

import org.example.estudebackendspring.dto.AssignmentSummaryDTO;
import org.example.estudebackendspring.dto.ClassDTO;
import org.example.estudebackendspring.dto.ClassSubjectDTO;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Enrollment;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.service.AssignmentSubmissionService;
import org.example.estudebackendspring.service.EnrollmentService;
import org.example.estudebackendspring.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final StudentRepository repository;
    private final AssignmentSubmissionService service;

    public StudentController(StudentService studentService, EnrollmentService enrollmentService,
                             StudentRepository repository, AssignmentSubmissionService service) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.repository = repository;
        this.service = service;
    }
    @GetMapping
    public List<Student> getAllStudents() {
        return repository.findAll();
    }
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable Long studentId) {
        return studentService.getStudentById(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    //List classes the student is enrolled in
    @GetMapping("/{studentId}/enrollments")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/by-class/{classId}")
    public ResponseEntity<List<Student>> getStudentsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(studentService.getStudentsByClass(classId));
    }


    @GetMapping("/by-school/{schoolId}")
    public ResponseEntity<List<Student>> getStudentsBySchool(@PathVariable Long schoolId) {
        return ResponseEntity.ok(studentService.getStudentsBySchool(schoolId));
    }
    @GetMapping("/{studentId}/assignments")
    public ResponseEntity<List<AssignmentSummaryDTO>> listAssignmentsForStudent(@PathVariable Long studentId) {
        List<AssignmentSummaryDTO> list = service.listAssignmentsForStudent(studentId);
        if (list.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{studentId}/classes")
    public ResponseEntity<?> getClassesByStudent(@PathVariable Long studentId) {
        List<Clazz> classes = studentService.getClassesByStudent(studentId);

        List<ClassDTO> result = classes.stream().map(clazz -> {
            // Map Term -> TermInfo
            List<ClassDTO.TermInfo> termInfos = clazz.getTerms().stream()
                    .map(term -> new ClassDTO.TermInfo(
                            term.getTermId(),
                            term.getName(),
                            term.getBeginDate(),
                            term.getEndDate()
                    ))
                    .toList();

            return new ClassDTO(
                    clazz.getClassId(),
                    clazz.getName(),
                    clazz.getGradeLevel(),
                    termInfos,
                    clazz.getEnrollments() != null ? clazz.getEnrollments().size() : 0,
                    clazz.getHomeroomTeacher() != null ? clazz.getHomeroomTeacher().getFullName() : null
            );
        }).toList();

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{studentId}/subjects")
    public ResponseEntity<?> getSubjects(@PathVariable Long studentId) {
        List<ClassSubject> subjects = studentService.getSubjectsByStudent(studentId);

        List<ClassSubjectDTO> dtoList = subjects.stream()
                .map(cs -> new ClassSubjectDTO(
                        cs.getClassSubjectId(),
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


}
