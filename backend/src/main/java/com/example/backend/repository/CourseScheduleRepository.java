package com.example.backend.repository;

import com.example.backend.entity.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

    List<CourseSchedule> findByCourseId(Long courseId);

    // 按星期几查询
    List<CourseSchedule> findByDayOfWeek(Integer dayOfWeek);

    // 查询某个学生已选课程的时间安排
    @Query("SELECT s FROM CourseSchedule s WHERE s.courseId IN " +
           "(SELECT e.courseId FROM CourseEnrollment e WHERE e.studentId = :studentId AND e.enrollmentStatus = 'ENROLLED')")
    List<CourseSchedule> findByStudentEnrolled(@Param("studentId") Long studentId);

    // 删除课程的所有时间安排
    void deleteByCourseId(Long courseId);
}
