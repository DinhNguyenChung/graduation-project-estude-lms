package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminCode(String adminCode);
}
