package com.example.backend.repository;

import com.example.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByParentId(Long parentId);

    @Query("SELECT d.id FROM Department d WHERE d.parentId = :parentId")
    List<Long> findChildIds(Long parentId);
}
