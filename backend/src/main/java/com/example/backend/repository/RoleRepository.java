package com.example.backend.repository;

import com.example.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId")
    List<Role> findByUserId(Long userId);

    Role findByCode(String code);
}
