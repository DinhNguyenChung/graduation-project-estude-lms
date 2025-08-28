package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNumberPhone(String numberPhone);
}


