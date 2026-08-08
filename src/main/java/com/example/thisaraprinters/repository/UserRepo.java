package com.example.thisaraprinters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.thisaraprinters.model.UserModel;

public interface UserRepo extends JpaRepository<UserModel, Integer> {
    // find by email
    UserModel findByUsername(String username);

    @Query(value = "SELECT id FROM users WHERE username = ?1", nativeQuery = true)
    Integer findIdByUsername(String username);

}
