package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.estudebackendspring.dto.SubjectGradeDTO;
import org.example.estudebackendspring.dto.SubjectGradeInfoDTO;
import org.example.estudebackendspring.dto.SubjectGradeRequest;
import org.example.estudebackendspring.dto.TermGradesDTO;
import org.example.estudebackendspring.entity.ClassSubject;
import org.example.estudebackendspring.entity.Student;
import org.example.estudebackendspring.entity.SubjectGrade;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClassSubjectRepository;
import org.example.estudebackendspring.repository.StudentRepository;
import org.example.estudebackendspring.repository.SubjectGradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectGradeService {
    private final SubjectGradeRepository subjectGradeRepository;
    private final StudentRepository studentRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private static final double REGULAR_WEIGHT = 0.10;
    private static final double MIDTERM_WEIGHT = 0.20;
    private static final double FINAL_WEIGHT = 0.30;
    
    @Transactional
    public List<SubjectGrade> getByClassSubject(Long classSubjectId) {
        return subjectGradeRepository.findByClassSubjectIdWithStudent(classSubjectId);
    }

    @Transactional
    public SubjectGrade updateSubjectGrade(Long subjectGradeId,
                                           List<Float> regularScores,
                                           Float midtermScore,
                                           Float finalScore,
                                           String comment) {
        SubjectGrade subjectGrade = subjectGradeRepository.findById(subjectGradeId)
                .orElseThrow(() -> new RuntimeException("SubjectGrade not found"));

        subjectGrade.setRegularScores(regularScores);
        subjectGrade.setMidtermScore(midtermScore);
        subjectGrade.setFinalScore(finalScore);
        subjectGrade.setComment(comment);

        // Tính average
        float sum = 0;
        if (regularScores != null && !regularScores.isEmpty()) {
            for (Float s : regularScores) sum += s;
            subjectGrade.setActualAverage((sum / regularScores.size() + midtermScore + finalScore) / 3);
        }

        return subjectGradeRepository.save(subjectGrade);
    }
    @Transactional
    public SubjectGradeDTO upsertSubjectGrade(SubjectGradeRequest req) {
        // Validate student and classSubject exist
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + req.getStudentId()));

        ClassSubject cs = classSubjectRepository.findById(req.getClassSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassSubject not found: " + req.getClassSubjectId()));

        // Find existing grade (student + classSubject)
        Optional<SubjectGrade> existingOpt = subjectGradeRepository
                .findByStudent_UserIdAndClassSubject_ClassSubjectId(req.getStudentId(), req.getClassSubjectId());

        SubjectGrade grade = existingOpt.orElseGet(SubjectGrade::new);

        // Set relations if created
        grade.setStudent(student);
        grade.setClassSubject(cs);

        // Update fields
        if (req.getRegularScores() != null) {
            grade.setRegularScores(new ArrayList<>(req.getRegularScores()));
        }
        grade.setMidtermScore(req.getMidtermScore());
        grade.setFinalScore(req.getFinalScore());
        grade.setComment(req.getComment());

        // Compute averages
        double regularAvg = computeRegularAverage(grade.getRegularScores()); // returns NaN if no regulars
        Float mid = grade.getMidtermScore();
        Float fin = grade.getFinalScore();

        // NEW RULE:
        // - If final (fin) is NOT provided -> actualAverage stays null (do NOT compute).
        // - If final IS provided -> treat missing mid or regular as 0 and compute using formula:
        //   actual = (1*regularAvg + 2*mid + 3*final) / 6
        Float actualAverage = null;
        if (fin != null) {
            double regularForCalc = Double.isNaN(regularAvg) ? 0.0 : regularAvg;
            double midForCalc = (mid == null) ? 0.0 : mid.doubleValue();
            double finalForCalc = fin.doubleValue();

            double raw = (1.0 * regularForCalc + 2.0 * midForCalc + 3.0 * finalForCalc) / 6.0;

            // Round to 2 decimal places
            BigDecimal bd = BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP);
            actualAverage = bd.floatValue();
        } else {
            actualAverage = null;
        }

        // DON'T set predicted fields here (AI handled elsewhere).
        grade.setActualAverage(actualAverage);
        // keep predicted fields unchanged

        SubjectGrade saved = subjectGradeRepository.save(grade);

        return toDto(saved);
    }



    @Transactional
    public SubjectGradeDTO getSubjectGrade(Long gradeId) {
        SubjectGrade g = subjectGradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("SubjectGrade not found: " + gradeId));
        return toDto(g);
    }

    @Transactional
    public Optional<SubjectGradeDTO> findByStudentAndClassSubject(Long studentId, Long classSubjectId) {
        return subjectGradeRepository
                .findByStudent_UserIdAndClassSubject_ClassSubjectId(studentId, classSubjectId)
                .map(this::toDto);
    }

    // Utils
    private double computeRegularAverage(List<Float> scores) {
        if (scores == null || scores.isEmpty()) return Double.NaN;
        double sum = 0.0;
        int count = 0;
        for (Float f : scores) {
            if (f != null) { sum += f; count++; }
        }
        return count == 0 ? Double.NaN : sum / count;
    }
    /**
     * Trả về tất cả bảng điểm của học sinh, nhóm theo kỳ (term).
     */
    public List<TermGradesDTO> getAllGradesGroupedByTerm(Long studentId) {
        List<SubjectGrade> grades = subjectGradeRepository.findAllByStudentIdFetchAll(studentId);

        // Lọc an toàn: chỉ lấy các record có ClassSubject + Term
        List<SubjectGrade> filtered = grades.stream()
                .filter(sg -> sg.getClassSubject() != null && sg.getClassSubject().getTerm() != null)
                .collect(Collectors.toList());

        // Group theo termId (dùng LinkedHashMap để giữ thứ tự)
        Map<Long, List<SubjectGrade>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(
                        sg -> sg.getClassSubject().getTerm().getTermId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TermGradesDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<SubjectGrade>> entry : grouped.entrySet()) {
            List<SubjectGrade> list = entry.getValue();
            var term = list.get(0).getClassSubject().getTerm();

            List<SubjectGradeInfoDTO> subjectDtos = list.stream()
                    .map(this::toSubjectGradeInfoDTO)
                    .collect(Collectors.toList());

            TermGradesDTO termDto = new TermGradesDTO(
                    term.getTermId(),
                    term.getName(),
                    term.getBeginDate(),
                    term.getEndDate(),
                    subjectDtos
            );
            result.add(termDto);
        }

        return result;
    }

    private SubjectGradeInfoDTO toSubjectGradeInfoDTO(SubjectGrade sg) {
        var cs = sg.getClassSubject();
        var subj = cs != null ? cs.getSubject() : null;
        var term = cs != null ? cs.getTerm() : null;
        var clazz = term != null ? term.getClazz() : null;
        var teacher = cs != null ? cs.getTeacher() : null;
        Float avg = sg.getActualAverage();
        String rank = calculateRank(avg);

        SubjectGradeInfoDTO dto = new SubjectGradeInfoDTO();
        dto.setSubjectGradeId(sg.getSubjectGradeId());
        dto.setRegularScores(sg.getRegularScores());
        dto.setMidtermScore(sg.getMidtermScore());
        dto.setFinalScore(sg.getFinalScore());
        dto.setActualAverage(sg.getActualAverage());
        dto.setComment(sg.getComment());
        dto.setRank(rank);

        dto.setClassSubjectId(cs != null ? cs.getClassSubjectId() : null);

        dto.setSubjectId(subj != null ? subj.getSubjectId() : null);
        dto.setSubjectName(subj != null ? subj.getName() : null);
        dto.setSubjectDescription(subj != null ? subj.getDescription() : null);

        dto.setTeacherId(teacher != null ? teacher.getUserId() : null);
        dto.setTeacherName(teacher != null ? teacher.getFullName() : null);

        dto.setClassId(clazz != null ? clazz.getClassId() : null);
        dto.setClassName(clazz != null ? clazz.getName() : null);

        return dto;
    }

    private SubjectGradeDTO toDto(SubjectGrade g) {
        Float avg = g.getActualAverage();
        String rank = calculateRank(avg);
        SubjectGradeDTO dto = new SubjectGradeDTO();
        dto.setSubjectGradeId(g.getSubjectGradeId());
        dto.setStudentId(g.getStudent() != null ? g.getStudent().getUserId() : null);
        dto.setClassSubjectId(g.getClassSubject() != null ? g.getClassSubject().getClassSubjectId() : null);
        dto.setRegularScores(g.getRegularScores());
        dto.setMidtermScore(g.getMidtermScore());
        dto.setFinalScore(g.getFinalScore());
        dto.setActualAverage(g.getActualAverage());
//        dto.setPredictedMidTerm(g.getPredictedMidTerm());
//        dto.setPredictedFinal(g.getPredictedFinal());
//        dto.setPredictedAverage(g.getPredictedAverage());
//        dto.setComment(g.getComment());
        dto.setRank(rank);
        return dto;
    }
    private String calculateRank(Float avg) {
        if (avg == null) return "";

        if (avg >= 8.0f) return "Tốt";
        if (avg >= 6.5f) return "Khá";
        if (avg >= 5.0f) return "Đạt";
        if (avg >= 3.5f) return "Kém";
        return "Chưa đạt";
    }

}