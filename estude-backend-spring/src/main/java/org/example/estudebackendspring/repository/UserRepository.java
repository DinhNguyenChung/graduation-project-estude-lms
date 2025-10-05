package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNumberPhone(String numberPhone);
    // All user ids (for SYSTEM) — adapt active filter if you have it
    @Query("SELECT u.userId FROM User u")
    List<Long> findAllUserIds();

    // Users in a school
    @Query("SELECT u.userId FROM User u WHERE u.school.schoolId = :schoolId")
    List<Long> findUserIdsBySchoolId(@Param("schoolId") Long schoolId);
}


