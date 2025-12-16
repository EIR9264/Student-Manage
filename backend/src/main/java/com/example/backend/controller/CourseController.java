package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.dto.CourseCreateRequest;
import com.example.backend.dto.CourseDTO;
import com.example.backend.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取课程列表
     */
    @GetMapping
    public ResponseEntity<?> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long studentId = getStudentIdFromRequest(request);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CourseDTO> courses = courseService.getCourses(keyword, status, studentId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", courses);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseDetail(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = getStudentIdFromRequest(request);
        CourseDTO course = courseService.getCourseDetail(id, studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", course);

        return ResponseEntity.ok(response);
    }

    /**
     * 创建课程（管理员）
     */
    @PostMapping
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseCreateRequest request,
                                          HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        CourseDTO course = courseService.createCourse(request, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "课程创建成功");
        response.put("data", course);

        return ResponseEntity.ok(response);
    }

    /**
     * 更新课程（管理员）
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id,
                                          @Valid @RequestBody CourseCreateRequest request) {
        CourseDTO course = courseService.updateCourse(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "课程更新成功");
        response.put("data", course);

        return ResponseEntity.ok(response);
    }

    /**
     * 删除课程（管理员）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "课程删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 更新课程状态（管理员）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCourseStatus(@PathVariable Long id,
                                                @RequestParam String status) {
        courseService.updateCourseStatus(id, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "课程状态更新成功");

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
