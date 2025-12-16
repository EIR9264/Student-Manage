package com.example.backend.repository;

import com.example.backend.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    // 查询学生的选课记录
    List<CourseEnrollment> findByStudentIdAndEnrollmentStatus(Long studentId, String status);

    // 查询学生的所有选课记录
    List<CourseEnrollment> findByStudentId(Long studentId);

    // 查询课程的选课学生
    Page<CourseEnrollment> findByCourseIdAndEnrollmentStatus(Long courseId, String status, Pageable pageable);

    // 查询特定学生是否已选某课程
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    // 检查是否已选课
    boolean existsByCourseIdAndStudentIdAndEnrollmentStatus(Long courseId, Long studentId, String status);

    // 统计课程已选人数
    long countByCourseIdAndEnrollmentStatus(Long courseId, String status);

    // 统计学生已选课程数
    long countByStudentIdAndEnrollmentStatus(Long studentId, String status);

    // 查询学生已选的课程ID列表
    @Query("SELECT e.courseId FROM CourseEnrollment e WHERE e.studentId = :studentId AND e.enrollmentStatus = 'ENROLLED'")
    List<Long> findEnrolledCourseIdsByStudentId(@Param("studentId") Long studentId);
}
