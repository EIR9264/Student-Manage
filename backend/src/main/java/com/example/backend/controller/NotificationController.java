package com.example.backend.controller;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<NotificationDTO> notifications = notificationService.getNotifications(
            userId, PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of("success", true, "data", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "已标记为已读"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "全部标记为已读"));
    }
}
