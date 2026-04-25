package com.example.demo.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByRoleId(Integer roleId);
    Role findByRoleName(String role);
}
