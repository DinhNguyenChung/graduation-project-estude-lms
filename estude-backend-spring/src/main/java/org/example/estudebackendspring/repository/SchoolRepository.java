package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySchoolCode(String schoolCode);
    List<School> getClazzBySchoolCode(String schoolCode);
    List<School> getClazzBySchoolId(Long schoolId);
    School findBySchoolId(Long schoolId);


}