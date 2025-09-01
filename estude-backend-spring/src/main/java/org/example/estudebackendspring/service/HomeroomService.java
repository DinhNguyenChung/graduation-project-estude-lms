package org.example.estudebackendspring.service;

import jakarta.transaction.Transactional;
import org.example.estudebackendspring.entity.Admin;
import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.exception.ResourceNotFoundException;
import org.example.estudebackendspring.exception.UnauthorizedException;
import org.example.estudebackendspring.repository.ClazzRepository;
import org.example.estudebackendspring.repository.TeacherRepository;
import org.example.estudebackendspring.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        // Nếu teacher đã là homeroom cho 1 lớp khác
        if (teacher.getHomeroomClass() != null &&
                !teacher.getHomeroomClass().getClassId().equals(clazz.getClassId())) {
            throw new IllegalStateException("Teacher is already homeroom for another class (id=" +
                    teacher.getHomeroomClass().getClassId() + ")");
        }

        // Nếu lớp đang có homeroom cũ
        Teacher old = clazz.getHomeroomTeacher();
        if (old != null && !old.getUserId().equals(teacher.getUserId())) {
            old.setHomeroomClass(null);
            old.setHomeroomTeacher(false); // cập nhật flag
            teacherRepository.save(old);
        }

        // Gán 2 chiều
        clazz.setHomeroomTeacher(teacher);
        teacher.setHomeroomClass(clazz);
        teacher.setHomeroomTeacher(true); // cập nhật flag

        teacherRepository.save(teacher);
        return clazzRepository.save(clazz);
    }

    @Transactional
    public Clazz removeHomeroomTeacher(Long actingUserId, Long classId) {
        checkPermission(actingUserId);

        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        Teacher homeroom = clazz.getHomeroomTeacher();
        if (homeroom == null) {
            throw new ResourceNotFoundException("This class has no homeroom teacher assigned");
        }

        // tháo 2 chiều
        clazz.setHomeroomTeacher(null);
        homeroom.setHomeroomClass(null);
        homeroom.setHomeroomTeacher(false); // cập nhật flag

        teacherRepository.save(homeroom);
        return clazzRepository.save(clazz);
    }

    @Transactional
    public Clazz updateHomeroomTeacher(Long actingUserId, Long classId, Long newTeacherId) {
        checkPermission(actingUserId);

        Clazz clazz = clazzRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        Teacher newTeacher = teacherRepository.findById(newTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + newTeacherId));

        // Nếu newTeacher đã homeroom cho lớp khác
        if (newTeacher.getHomeroomClass() != null &&
                !newTeacher.getHomeroomClass().getClassId().equals(clazz.getClassId())) {
            throw new IllegalStateException("Teacher is already homeroom for another class (id=" +
                    newTeacher.getHomeroomClass().getClassId() + ")");
        }

        Teacher old = clazz.getHomeroomTeacher();
        if (old != null && !old.getUserId().equals(newTeacher.getUserId())) {
            old.setHomeroomClass(null);
            old.setHomeroomTeacher(false); // cập nhật flag
            teacherRepository.save(old);
        }

        clazz.setHomeroomTeacher(newTeacher);
        newTeacher.setHomeroomClass(clazz);
        newTeacher.setHomeroomTeacher(true); // cập nhật flag

        teacherRepository.save(newTeacher);
        return clazzRepository.save(clazz);
    }
}