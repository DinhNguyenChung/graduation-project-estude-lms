package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Clazz;
import org.example.estudebackendspring.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, Long> {
    /**
     * Kiểm tra xem có tồn tại Clazz với tên và trường học cụ thể hay không.
     */
    boolean existsByNameAndSchool(String name, School school);

    /**
     * Tìm Clazz theo tên và trường học.
     */
    Optional<Clazz> findByNameAndSchool(String name, School school);
    List<Clazz> findBySchool_SchoolId(Long schoolId);
    List<Clazz> findByHomeroomTeacher_UserId(Long teacherId);


}