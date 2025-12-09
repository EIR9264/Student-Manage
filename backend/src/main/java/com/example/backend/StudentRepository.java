package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // 可以在这里添加自定义查询，例如按学号查找
    // Optional<Student> findByStudentNumber(String studentNumber);
}