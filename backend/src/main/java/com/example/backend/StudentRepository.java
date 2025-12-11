package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // 关键：JPA 会自动根据这个方法名生成 SQL:
    // WHERE name LIKE %?1% OR student_number LIKE %?2%
    List<Student> findByNameContainingOrStudentNumberContaining(String name, String studentNumber);
}