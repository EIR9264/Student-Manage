package com.example.backend.controller;

import com.example.backend.dto.UpdatePasswordRequest;
import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取当前用户信息
    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getUserById(userId);
            result.put("success", true);
            result.put("data", userService.toUserInfo(user));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 更新个人信息
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.updateProfile(userId, request);
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("data", userService.toUserInfo(user));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 修改密码
    @PutMapping("/password")
    public Map<String, Object> updatePassword(@RequestBody UpdatePasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        Map<String, Object> result = new HashMap<>();
        try {
            userService.updatePassword(userId, request);
            result.put("success", true);
            result.put("message", "密码修改成功，请重新登录");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 获取所有用户列表（仅管理员）
    @GetMapping("/list")
    public Map<String, Object> getUserList(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Map<String, Object> result = new HashMap<>();

        if (!"ADMIN".equals(role)) {
            result.put("success", false);
            result.put("message", "权限不足");
            return result;
        }

        try {
            result.put("success", true);
            result.put("data", userService.getAllUsers());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 修改用户角色（仅管理员，且不能修改admin和自己）
    @PutMapping("/{targetUserId}/role")
    public Map<String, Object> updateUserRole(
            @PathVariable Long targetUserId,
            @RequestBody Map<String, String> requestData,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("role");
        String newRole = requestData.get("role");

        Map<String, Object> result = new HashMap<>();

        // 1. 检查当前用户是否是管理员
        if (!"ADMIN".equals(currentRole)) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可操作");
            return result;
        }

        // 2. 不能修改自己的角色
        if (currentUserId.equals(targetUserId)) {
            result.put("success", false);
            result.put("message", "不能修改自己的角色");
            return result;
        }

        try {
            userService.updateUserRole(currentUserId, targetUserId, newRole);
            result.put("success", true);
            result.put("message", "角色修改成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
