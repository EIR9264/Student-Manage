package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.MinioService;
import com.example.backend.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        try {
            // 上传到MinIO
            String objectName = minioService.uploadAvatar(file, userId);

            // 更新学生头像
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (user.getStudentId() != null) {
                studentService.updateAvatar(user.getStudentId(), objectName);
            }

            // 返回头像URL
            String avatarUrl = minioService.getAvatarUrl(objectName);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "头像上传成功",
                    "data", Map.of(
                            "avatarUrl", avatarUrl,
                            "objectName", objectName
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/url")
    public ResponseEntity<?> getAvatarUrl(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String avatarPath = null;
            if (user.getStudentId() != null) {
                avatarPath = studentService.getAvatarPath(user.getStudentId());
            }

            String avatarUrl = minioService.getAvatarUrl(avatarPath);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "avatarUrl", avatarUrl != null ? avatarUrl : ""
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
