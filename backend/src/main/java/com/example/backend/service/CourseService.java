package com.example.backend.service;

import com.example.backend.dto.CourseCreateRequest;
import com.example.backend.dto.CourseDTO;
import com.example.backend.dto.ScheduleDTO;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseSchedule;
import com.example.backend.repository.CourseAttachmentRepository;
import com.example.backend.repository.CourseEnrollmentRepository;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.CourseScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseScheduleRepository scheduleRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseAttachmentRepository attachmentRepository;

    /**
     * 获取课程列表（分页）
     */
    public Page<CourseDTO> getCourses(String keyword, String status, Long studentId, Pageable pageable) {
        Page<Course> courses;

        if (keyword != null && !keyword.isEmpty()) {
            courses = courseRepository.searchCourses(status != null ? status : "ACTIVE", keyword, pageable);
        } else {
            courses = courseRepository.findByStatus(status != null ? status : "ACTIVE", pageable);
        }

        // 获取学生已选课程ID列表
        List<Long> enrolledCourseIds = studentId != null ?
                enrollmentRepository.findEnrolledCourseIdsByStudentId(studentId) : List.of();

        return courses.map(course -> {
            CourseDTO dto = CourseDTO.fromEntity(course);
            dto.setEnrolled(enrolledCourseIds.contains(course.getId()));
            dto.setAttachmentCount((int) attachmentRepository.countByCourseId(course.getId()));
            return dto;
        });
    }

    /**
     * 获取课程详情
     */
    public CourseDTO getCourseDetail(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        CourseDTO dto = CourseDTO.fromEntity(course);

        // 获取时间安排
        List<CourseSchedule> schedules = scheduleRepository.findByCourseId(courseId);
        dto.setSchedules(schedules.stream().map(ScheduleDTO::fromEntity).collect(Collectors.toList()));

        // 检查是否已选
        if (studentId != null) {
            dto.setEnrolled(enrollmentRepository.existsByCourseIdAndStudentIdAndEnrollmentStatus(
                    courseId, studentId, "ENROLLED"));
        }

        dto.setAttachmentCount((int) attachmentRepository.countByCourseId(courseId));

        return dto;
    }

    /**
     * 创建课程
     */
    @Transactional
    public CourseDTO createCourse(CourseCreateRequest request, Long createdBy) {
        // 检查课程编号是否已存在
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new RuntimeException("课程编号已存在");
        }

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setTeacherName(request.getTeacherName());
        course.setCredit(request.getCredit());
        course.setMaxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 0);
        course.setCourseType(request.getCourseType() != null ? request.getCourseType() : "REQUIRED");
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setCoverImage(request.getCoverImage());
        course.setCreatedBy(createdBy);
        course.setStatus("ACTIVE");

        course = courseRepository.save(course);

        // 保存时间安排
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            Long courseId = course.getId();
            for (ScheduleDTO scheduleDTO : request.getSchedules()) {
                CourseSchedule schedule = scheduleDTO.toEntity();
                schedule.setCourseId(courseId);
                scheduleRepository.save(schedule);
            }
        }

        return getCourseDetail(course.getId(), null);
    }

    /**
     * 更新课程
     */
    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseCreateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 如果修改了课程编号，检查是否冲突
        if (!course.getCourseCode().equals(request.getCourseCode()) &&
                courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new RuntimeException("课程编号已存在");
        }

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setTeacherName(request.getTeacherName());
        course.setCredit(request.getCredit());
        course.setMaxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 0);
        course.setCourseType(request.getCourseType());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setCoverImage(request.getCoverImage());

        courseRepository.save(course);

        // 更新时间安排：先删除旧的，再添加新的
        if (request.getSchedules() != null) {
            scheduleRepository.deleteByCourseId(courseId);
            for (ScheduleDTO scheduleDTO : request.getSchedules()) {
                CourseSchedule schedule = scheduleDTO.toEntity();
                schedule.setId(null); // 确保是新建
                schedule.setCourseId(courseId);
                scheduleRepository.save(schedule);
            }
        }

        return getCourseDetail(courseId, null);
    }

    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("课程不存在");
        }

        // 检查是否有学生选课
        long enrolledCount = enrollmentRepository.countByCourseIdAndEnrollmentStatus(courseId, "ENROLLED");
        if (enrolledCount > 0) {
            throw new RuntimeException("该课程还有" + enrolledCount + "名学生选课，无法删除");
        }

        // 删除时间安排
        scheduleRepository.deleteByCourseId(courseId);

        // 删除课程（附件需要单独处理）
        courseRepository.deleteById(courseId);
    }

    /**
     * 更新课程状态
     */
    public void updateCourseStatus(Long courseId, String status) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        course.setStatus(status);
        courseRepository.save(course);
    }
}
