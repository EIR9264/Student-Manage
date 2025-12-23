package com.example.backend.controller;

import com.example.backend.entity.ClassInfo;
import com.example.backend.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class ClassInfoController {

    @Autowired
    private ClassInfoService classInfoService;

    @GetMapping
    public List<ClassInfo> getAllClasses() {
        return classInfoService.getAllClasses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClassById(@PathVariable Long id) {
        return classInfoService.getClassById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createClass(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "班级名称不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            ClassInfo classInfo = classInfoService.createClass(name.trim());
            response.put("success", true);
            response.put("data", classInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateClass(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "班级名称不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            ClassInfo classInfo = classInfoService.updateClass(id, name.trim());
            response.put("success", true);
            response.put("data", classInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteClass(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            classInfoService.deleteClass(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/student-count")
    public ResponseEntity<Map<String, Object>> getStudentCount(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return classInfoService.getClassById(id)
                .map(classInfo -> {
                    long count = classInfoService.getStudentCount(classInfo.getName());
                    response.put("count", count);
                    response.put("hasStudents", count > 0);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
