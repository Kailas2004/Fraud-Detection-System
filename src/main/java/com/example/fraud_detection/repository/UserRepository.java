package com.example.fraud_detection.repository;

import com.example.fraud_detection.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByIsActive(Boolean isActive);
    
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %?1%")
    List<User> findByFullNameContaining(String name);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}