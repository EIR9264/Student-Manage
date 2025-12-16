package com.example.backend.repository;

import com.example.backend.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    // 按状态查询
    List<Course> findByStatus(String status);

    // 分页查询活跃课程
    Page<Course> findByStatus(String status, Pageable pageable);

    // 按课程名称或编号模糊搜索
    @Query("SELECT c FROM Course c WHERE c.status = :status AND " +
           "(c.courseName LIKE %:keyword% OR c.courseCode LIKE %:keyword% OR c.teacherName LIKE %:keyword%)")
    Page<Course> searchCourses(@Param("status") String status, @Param("keyword") String keyword, Pageable pageable);

    // 乐观锁更新选课人数（防止超卖）
    @Modifying
    @Query("UPDATE Course c SET c.currentStudents = c.currentStudents + 1 " +
           "WHERE c.id = :courseId AND (c.maxStudents = 0 OR c.currentStudents < c.maxStudents)")
    int incrementCurrentStudents(@Param("courseId") Long courseId);

    // 减少选课人数（退课时）
    @Modifying
    @Query("UPDATE Course c SET c.currentStudents = c.currentStudents - 1 " +
           "WHERE c.id = :courseId AND c.currentStudents > 0")
    int decrementCurrentStudents(@Param("courseId") Long courseId);
}
