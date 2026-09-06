package com.collegestore.unistore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.collegestore.unistore.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}