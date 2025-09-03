package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, Long> {
    boolean existsByNameAndTerm(String name, String term);
    List<Clazz> findBySchool_SchoolId(Long schoolId);


}