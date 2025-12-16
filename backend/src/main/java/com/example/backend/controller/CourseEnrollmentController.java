package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.dto.CalendarEventDTO;
import com.example.backend.dto.EnrollmentDTO;
import com.example.backend.service.CourseEnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class CourseEnrollmentController {

    @Autowired
    private CourseEnrollmentService enrollmentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 选课
     */
    @PostMapping
    public ResponseEntity<?> enrollCourse(@RequestBody Map<String, Long> request,
                                          HttpServletRequest httpRequest) {
        Long courseId = request.get("courseId");
        if (courseId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "课程ID不能为空"));
        }

        Long userId = getUserIdFromRequest(httpRequest);
        Long studentId = getStudentIdFromRequest(httpRequest);

        if (studentId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "您不是学生用户，无法选课"));
        }

        EnrollmentDTO enrollment = enrollmentService.enrollCourse(courseId, studentId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "选课成功");
        response.put("data", enrollment);

        return ResponseEntity.ok(response);
    }

    /**
     * 退课
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> dropCourse(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);

        if (studentId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "您不是学生用户"));
        }

        enrollmentService.dropCourse(id, studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "退课成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取我的选课列表
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyEnrollments(HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);

        if (studentId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "您不是学生用户"));
        }

        List<EnrollmentDTO> enrollments = enrollmentService.getMyEnrollments(studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", enrollments);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取课程日历数据
     */
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        Long studentId = getStudentIdFromRequest(request);

        if (studentId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "您不是学生用户"));
        }

        List<CalendarEventDTO> events = enrollmentService.getCalendarEvents(studentId, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", events);

        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Long getStudentIdFromRequest(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return null;

        return userRepository.findById(userId)
                .map(User::getStudentId)
                .orElse(null);
    }
}
