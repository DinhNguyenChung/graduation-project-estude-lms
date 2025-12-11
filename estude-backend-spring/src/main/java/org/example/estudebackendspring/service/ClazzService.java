package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.dto.ClazzDTO;
import org.example.estudebackendspring.dto.CreateClazzRequest;
import org.example.estudebackendspring.dto.TermDTO;
import org.example.estudebackendspring.dto.UpdateClazzRequest;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.School;
import org.example.estudebackendspring.entity.Term;
import org.example.estudebackendspring.exception.DuplicateResourceException;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.SchoolRepository;
import org.example.estudebackendspring.repository.TermRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClazzService {
    private final ClazzRepository clazzRepository;
    private final SchoolRepository schoolRepository;
    private final TermRepository termRepository;

    public ClazzService(ClazzRepository clazzRepository, SchoolRepository schoolRepository, TermRepository termRepository) {
        this.clazzRepository = clazzRepository;
        this.schoolRepository = schoolRepository;
        this.termRepository = termRepository;
    }
    public Clazz createClazz(CreateClazzRequest req) {
        // Tìm trường học
        School school = schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + req.getSchoolId()));

        // Kiểm tra trùng lặp tên lớp trong trường
//        if (clazzRepository.existsByNameAndSchool(req.getName(), school)) {
//            throw new DuplicateResourceException("Class with name " + req.getName() + " already exists in this school");
//        }

        // Tạo Clazz
        Clazz clazz = new Clazz();
        clazz.setName(req.getName());
        clazz.setGradeLevel(req.getGradeLevel()); // Gán gradeLevel
        clazz.setClassSize(req.getClassSize());
        clazz.setSchool(school);

        // Tạo danh sách Term (Kỳ 1, Kỳ 2)
        if (req.getTerms() != null && !req.getTerms().isEmpty()) {
            List<Term> terms = new ArrayList<>();
            for (CreateClazzRequest.TermInfo termInfo : req.getTerms()) {
                // Validate ngày
                if (termInfo.getBeginDate() != null && termInfo.getEndDate() != null
                        && termInfo.getBeginDate().after(termInfo.getEndDate())) {
                    throw new IllegalArgumentException("Begin date must be before end date for term " + termInfo.getName());
                }

//                // Validate tên kỳ
//                if (!termInfo.getName().equals("KY_1") && !termInfo.getName().equals("KY_2")) {
//                    throw new IllegalArgumentException("Term name must be KY_1 or KY_2");
//                }

                Term term = new Term();
                term.setName(termInfo.getName());
                term.setBeginDate(termInfo.getBeginDate());
                term.setEndDate(termInfo.getEndDate());
                term.setClazz(clazz);
                terms.add(term);
            }
            clazz.setTerms(terms);
        }

        // Lưu Clazz (và các Term liên quan nhờ cascade)
        return clazzRepository.save(clazz);
    }

    public Clazz getClazz(Long classId) {
        return clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
    }

    public Clazz updateClazz(Long classId, UpdateClazzRequest req) {
             // Tìm Clazz
        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        // Tìm trường học
        School school = schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + req.getSchoolId()));

        // Kiểm tra trùng lặp tên lớp
        if (!clazz.getName().equals(req.getName())) {
            if (clazzRepository.existsByNameAndSchool(req.getName(), school)) {
                throw new DuplicateResourceException("Another class with name " + req.getName() + " already exists in this school");
            }
        }

        // Cập nhật thông tin Clazz
        clazz.setName(req.getName());
        clazz.setGradeLevel(req.getGradeLevel()); // Gán gradeLevel
        clazz.setClassSize(req.getClassSize());
        clazz.setSchool(school);

        // Cập nhật hoặc thêm mới Term
        if (req.getTerms() != null && !req.getTerms().isEmpty()) {
            if (req.getTerms().size() > 2) {
                throw new IllegalArgumentException("A class can have at most 2 terms");
            }

            // Kiểm tra trùng lặp tên kỳ
            Set<String> termNames = req.getTerms().stream()
                    .map(UpdateClazzRequest.TermInfo::getName)
                    .collect(Collectors.toSet());
            if (termNames.size() != req.getTerms().size()) {
                throw new IllegalArgumentException("Duplicate term names are not allowed");
            }

            List<Term> updatedTerms = new ArrayList<>();
            Term previousTerm = null;
            for (UpdateClazzRequest.TermInfo termInfo : req.getTerms()) {
                if (termInfo.getBeginDate().after(termInfo.getEndDate())) {
                    throw new IllegalArgumentException("Begin date must be before end date for term " + termInfo.getName());
                }

                Term term;
                if (termInfo.getTermId() != null) {
                    // Cập nhật Term hiện có
                    term = termRepository.findById(termInfo.getTermId())
                            .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + termInfo.getTermId()));
                    if (!term.getClazz().getClassId().equals(classId)) {
                        throw new IllegalArgumentException("Term does not belong to this class");
                    }
                } else {
                    // Tạo Term mới
                    term = new Term();
                    term.setClazz(clazz);
                }

                term.setName(termInfo.getName());
                term.setBeginDate(termInfo.getBeginDate());
                term.setEndDate(termInfo.getEndDate());

                // Kiểm tra thời gian kỳ không chồng lấn
                if (previousTerm != null && !previousTerm.getEndDate().before(term.getBeginDate())) {
                    throw new IllegalArgumentException("Terms cannot overlap in time");
                }
                previousTerm = term;

                updatedTerms.add(term);
            }
            clazz.setTerms(updatedTerms);
        } else {
            // Giữ nguyên danh sách Term hiện tại nếu req.getTerms() rỗng
//            logger.info("No terms provided, keeping existing terms for class: {}", classId);
        }

        try {
            return clazzRepository.save(clazz);
        } catch (Exception e) {
//            logger.error("Failed to update class: {}", e.getMessage());

            throw e;
        }
    }

    public void deleteClazz(Long classId) {
        if (!clazzRepository.existsById(classId)) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }
        clazzRepository.deleteById(classId);
    }
    public List<Clazz> getClassesBySchool(Long schoolId) {
        return clazzRepository.findBySchool_SchoolId(schoolId);
    }
    
    /**
     * Get all classes as DTOs to avoid lazy loading issues
     */
    public List<ClazzDTO> getAllClasses() {
        List<Clazz> classes = clazzRepository.findAllWithRelationships();
        return classes.stream().map(clazz -> {
            ClazzDTO dto = new ClazzDTO();
            dto.setClassId(clazz.getClassId());
            dto.setName(clazz.getName());
            dto.setGradeLevel(clazz.getGradeLevel());
            dto.setClassSize(clazz.getClassSize());
            
            // Safely access eagerly-fetched relationships
            // Teacher extends User, so we can access fullName directly
            if (clazz.getHomeroomTeacher() != null) {
                dto.setHomeroomTeacherId(clazz.getHomeroomTeacher().getUserId());
                dto.setHomeroomTeacherName(clazz.getHomeroomTeacher().getFullName());
            }
            
            if (clazz.getSchool() != null) {
                dto.setSchoolId(clazz.getSchool().getSchoolId());
                dto.setSchoolName(clazz.getSchool().getSchoolName());
            }
            
            // Map terms to DTOs
            if (clazz.getTerms() != null) {
                List<TermDTO> termDTOs = clazz.getTerms().stream()
                    .map(term -> new TermDTO(
                        term.getTermId(),
                        term.getName(),
                        term.getBeginDate(),
                        term.getEndDate()
                    ))
                    .collect(Collectors.toList());
                dto.setTerms(termDTOs);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}
