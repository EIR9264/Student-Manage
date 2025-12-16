package com.example.backend.service;

import com.example.backend.entity.Student;
import com.example.backend.repository.StudentRepository;
import com.example.backend.dto.CalendarEventDTO;
import com.example.backend.dto.EnrollmentDTO;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseEnrollment;
import com.example.backend.entity.CourseSchedule;
import com.example.backend.repository.CourseEnrollmentRepository;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.CourseScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CourseEnrollmentService {

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseScheduleRepository scheduleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Value("${enrollment.max-courses:10}")
    private int maxCoursesPerStudent;

    /**
     * 学生选课
     */
    @Transactional
    public EnrollmentDTO enrollCourse(Long courseId, Long studentId, Long userId) {
        // 检查课程是否存在且状态正常
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!"ACTIVE".equals(course.getStatus())) {
            throw new RuntimeException("该课程当前不可选");
        }

        // 检查是否已选
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(courseId, studentId, "ENROLLED")) {
            throw new RuntimeException("您已选择该课程");
        }

        // 检查选课数量限制
        long enrolledCount = enrollmentRepository.countByStudentIdAndEnrollmentStatus(studentId, "ENROLLED");
        if (enrolledCount >= maxCoursesPerStudent) {
            throw new RuntimeException("选课数量已达上限（最多" + maxCoursesPerStudent + "门）");
        }

        // 检查课程容量（乐观锁）
        if (course.getMaxStudents() > 0) {
            int updated = courseRepository.incrementCurrentStudents(courseId);
            if (updated == 0) {
                throw new RuntimeException("该课程选课人数已满");
            }
        }

        // 创建选课记录
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setUserId(userId);
        enrollment.setEnrollmentStatus("ENROLLED");

        enrollment = enrollmentRepository.save(enrollment);

        // 构建返回DTO
        EnrollmentDTO dto = EnrollmentDTO.fromEntity(enrollment);
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setTeacherName(course.getTeacherName());
        dto.setCredit(course.getCredit());

        return dto;
    }

    /**
     * 退课
     */
    @Transactional
    public void dropCourse(Long enrollmentId, Long studentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在"));

        // 验证是否是本人的选课记录
        if (!enrollment.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权操作此选课记录");
        }

        if (!"ENROLLED".equals(enrollment.getEnrollmentStatus())) {
            throw new RuntimeException("该选课记录状态不允许退课");
        }

        // 更新状态为已退
        enrollment.setEnrollmentStatus("DROPPED");
        enrollment.setDroppedAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        // 减少课程选课人数
        courseRepository.decrementCurrentStudents(enrollment.getCourseId());
    }

    /**
     * 获取学生的选课列表
     */
    public List<EnrollmentDTO> getMyEnrollments(Long studentId) {
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudentIdAndEnrollmentStatus(studentId, "ENROLLED");

        List<EnrollmentDTO> result = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            EnrollmentDTO dto = EnrollmentDTO.fromEntity(enrollment);

            // 填充课程信息
            courseRepository.findById(enrollment.getCourseId()).ifPresent(course -> {
                dto.setCourseCode(course.getCourseCode());
                dto.setCourseName(course.getCourseName());
                dto.setTeacherName(course.getTeacherName());
                dto.setCredit(course.getCredit());
            });

            result.add(dto);
        }

        return result;
    }

    /**
     * 获取课程日历数据
     */
    public List<CalendarEventDTO> getCalendarEvents(Long studentId, LocalDate startDate, LocalDate endDate) {
        // 获取学生已选课程的时间安排
        List<CourseSchedule> schedules = scheduleRepository.findByStudentEnrolled(studentId);

        // 获取课程信息映射
        Map<Long, Course> courseMap = new HashMap<>();
        for (CourseSchedule schedule : schedules) {
            if (!courseMap.containsKey(schedule.getCourseId())) {
                courseRepository.findById(schedule.getCourseId()).ifPresent(course ->
                        courseMap.put(course.getId(), course));
            }
        }

        // 生成日历事件
        List<CalendarEventDTO> events = new ArrayList<>();
        String[] colors = {"#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399"};
        int colorIndex = 0;
        Map<Long, String> courseColors = new HashMap<>();
        long eventId = 0; // 用于生成唯一的事件ID

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = current.getDayOfWeek().getValue(); // 1-7

            for (CourseSchedule schedule : schedules) {
                if (schedule.getDayOfWeek().equals(dayOfWeek)) {
                    Course course = courseMap.get(schedule.getCourseId());
                    if (course == null) continue;

                    // 检查当前日期是否在课程有效期内
                    if (course.getStartDate() != null && current.isBefore(course.getStartDate())) {
                        continue;
                    }
                    if (course.getEndDate() != null && current.isAfter(course.getEndDate())) {
                        continue;
                    }

                    // 分配颜色
                    if (!courseColors.containsKey(course.getId())) {
                        courseColors.put(course.getId(), colors[colorIndex % colors.length]);
                        colorIndex++;
                    }

                    CalendarEventDTO event = new CalendarEventDTO();
                    event.setId(++eventId); // 使用递增的唯一ID
                    event.setTitle(course.getCourseName());
                    event.setStart(LocalDateTime.of(current, schedule.getStartTime()));
                    event.setEnd(LocalDateTime.of(current, schedule.getEndTime()));
                    event.setColor(courseColors.get(course.getId()));

                    Map<String, Object> props = new HashMap<>();
                    props.put("courseId", course.getId());
                    props.put("classroom", schedule.getClassroom());
                    props.put("teacherName", course.getTeacherName());
                    props.put("scheduleId", schedule.getId()); // 保留原始的schedule ID
                    event.setExtendedProps(props);

                    events.add(event);
                }
            }

            current = current.plusDays(1);
        }

        return events;
    }
}
