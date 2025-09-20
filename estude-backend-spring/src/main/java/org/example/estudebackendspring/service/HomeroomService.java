package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.entity.*;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.exception.UnauthorizedException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HomeroomService {
    private final ClazzRepository clazzRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;


    public HomeroomService(ClazzRepository clazzRepository,
                           TeacherRepository teacherRepository,
                           UserRepository userRepository) {
        this.clazzRepository = clazzRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    private void checkPermission(Long actingUserId) {
        User user = userRepository.findById(actingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + actingUserId));

        if (!(user instanceof Admin) && (!(user instanceof Teacher) || !((Teacher) user).isAdmin())) {
            throw new AccessDeniedException("You do not have permission to assign homeroom teachers");
        }
    }

    @Transactional
    public Clazz assignHomeroomTeacher(Long actingUserId, Long classId, Long teacherId) {
        checkPermission(actingUserId);

        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp có id: " + classId));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên có id: " + teacherId));

        // Lấy range thời gian của lớp (beginDate của Term đầu tiên, endDate của Term cuối cùng)
        List<Term> terms = clazz.getTerms();
        if (terms == null || terms.isEmpty()) {
            throw new IllegalStateException("Lớp không có bất kỳ dữ liệu về kỳ nào được định nghĩa");
        }

        Date minBeginDate = terms.stream()
                .map(Term::getBeginDate)
                .min(Date::compareTo)
                .orElseThrow();
        Date maxEndDate = terms.stream()
                .map(Term::getEndDate)
                .max(Date::compareTo)
                .orElseThrow();

        // Kiểm tra giáo viên này có chủ nhiệm lớp nào khác trong khoảng thời gian đó không
        List<Clazz> teacherClasses = clazzRepository.findByHomeroomTeacher_UserId(teacherId);
        for (Clazz other : teacherClasses) {
            if (!other.getClassId().equals(clazz.getClassId())) {
                List<Term> otherTerms = other.getTerms();
                if (otherTerms != null && !otherTerms.isEmpty()) {
                    Date otherBegin = otherTerms.stream()
                            .map(Term::getBeginDate)
                            .min(Date::compareTo)
                            .orElseThrow();
                    Date otherEnd = otherTerms.stream()
                            .map(Term::getEndDate)
                            .max(Date::compareTo)
                            .orElseThrow();

                    // Nếu giao nhau (overlap) → không cho phép
                    boolean overlap = !(maxEndDate.before(otherBegin) || minBeginDate.after(otherEnd));
                    if (overlap) {
                        throw new IllegalStateException("Giáo viên đã là chủ nhiệm của lớp khác trong cùng thời gian học kỳ");
                    }
                }
            }
        }

        // Nếu lớp đã có homeroom teacher cũ → bỏ
        Teacher old = clazz.getHomeroomTeacher();
        if (old != null && !old.getUserId().equals(teacher.getUserId())) {
            clazz.setHomeroomTeacher(null);
        }

        // Gán giáo viên mới
        clazz.setHomeroomTeacher(teacher);

        return clazzRepository.save(clazz);
    }


    @Transactional
    public Clazz removeHomeroomTeacher(Long actingUserId, Long classId) {
        checkPermission(actingUserId);

        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (clazz.getHomeroomTeacher() == null) {
            throw new ResourceNotFoundException("Lớp này không có giáo viên chủ nhiệm được phân công");
        }

        // Bỏ liên kết
        clazz.setHomeroomTeacher(null);

        return clazzRepository.save(clazz);
    }

    @Transactional
    public Clazz updateHomeroomTeacher(Long actingUserId, Long classId, Long newTeacherId) {
        checkPermission(actingUserId);

        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        Teacher newTeacher = teacherRepository.findById(newTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + newTeacherId));

        // Lấy range thời gian của lớp này
        List<Term> terms = clazz.getTerms();
        if (terms == null || terms.isEmpty()) {
            throw new IllegalStateException("Class does not have any terms defined");
        }

        Date minBeginDate = terms.stream()
                .map(Term::getBeginDate)
                .min(Date::compareTo)
                .orElseThrow();
        Date maxEndDate = terms.stream()
                .map(Term::getEndDate)
                .max(Date::compareTo)
                .orElseThrow();

        // Kiểm tra newTeacher có đang chủ nhiệm lớp khác trong thời gian này không
        List<Clazz> teacherClasses = clazzRepository.findByHomeroomTeacher_UserId(newTeacherId);
        for (Clazz other : teacherClasses) {
            if (!other.getClassId().equals(clazz.getClassId())) {
                List<Term> otherTerms = other.getTerms();
                if (otherTerms != null && !otherTerms.isEmpty()) {
                    Date otherBegin = otherTerms.stream()
                            .map(Term::getBeginDate)
                            .min(Date::compareTo)
                            .orElseThrow();
                    Date otherEnd = otherTerms.stream()
                            .map(Term::getEndDate)
                            .max(Date::compareTo)
                            .orElseThrow();

                    boolean overlap = !(maxEndDate.before(otherBegin) || minBeginDate.after(otherEnd));
                    if (overlap) {
                        throw new IllegalStateException("Giáo viên đã là chủ nhiệm của lớp khác trong cùng thời gian học kỳ");
                    }
                }
            }
        }

        // Nếu lớp đã có giáo viên khác thì gỡ
        Teacher old = clazz.getHomeroomTeacher();
        if (old != null && !old.getUserId().equals(newTeacher.getUserId())) {
            clazz.setHomeroomTeacher(null);
        }

        // Gán giáo viên mới
        clazz.setHomeroomTeacher(newTeacher);

        return clazzRepository.save(clazz);
    }

}