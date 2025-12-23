package com.example.backend.repository;

import com.example.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // 关键：JPA 会自动根据这个方法名生成 SQL:
    // WHERE name LIKE %?1% OR student_number LIKE %?2%
    List<Student> findByNameContainingOrStudentNumberContaining(String name, String studentNumber);

    // 按班级查询
    List<Student> findByClassName(String className);

    // 班级 + 关键词组合查询
    // WHERE class_name = ?1 AND (name LIKE %?2% OR student_number LIKE %?3%)
    List<Student> findByClassNameAndNameContainingOrClassNameAndStudentNumberContaining(
        String className1, String name, String className2, String studentNumber);

    // 统计班级学生数量
    long countByClassName(String className);
}
