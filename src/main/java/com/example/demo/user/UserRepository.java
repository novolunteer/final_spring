package com.example.demo.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"userRoles"})
    @Query("select u from User u where u.email=:email")
    User getWithRoles(@Param("email") String email);

    User findByUserId(Long userId);
}
