package com.example.backend.controller;

import com.example.backend.dto.AttachmentDTO;
import com.example.backend.dto.SearchResultDTO;
import com.example.backend.service.CourseAttachmentService;
import com.example.backend.service.ElasticsearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CourseAttachmentController {

    @Autowired
    private CourseAttachmentService attachmentService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 上传附件（管理员）
     */
    @PostMapping("/courses/{courseId}/attachments")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        AttachmentDTO attachment = attachmentService.uploadAttachment(courseId, file, sortOrder, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "附件上传成功");
        response.put("data", attachment);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取课程附件列表
     */
    @GetMapping("/courses/{courseId}/attachments")
    public ResponseEntity<?> getAttachments(@PathVariable Long courseId) {
        List<AttachmentDTO> attachments = attachmentService.getAttachmentsByCourse(courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", attachments);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取附件详情
     */
    @GetMapping("/attachments/{id}")
    public ResponseEntity<?> getAttachment(@PathVariable Long id) {
        AttachmentDTO attachment = attachmentService.getAttachment(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", attachment);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取预览URL
     */
    @GetMapping("/attachments/{id}/preview")
    public ResponseEntity<?> getPreviewUrl(@PathVariable Long id) {
        String previewUrl = attachmentService.getPreviewUrl(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", previewUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取下载URL
     */
    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<?> getDownloadUrl(@PathVariable Long id) {
        String downloadUrl = attachmentService.getDownloadUrl(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", downloadUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 删除附件（管理员）
     */
    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "附件删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 全文搜索附件
     */
    @GetMapping("/attachments/search")
    public ResponseEntity<?> searchAttachments(
            @RequestParam String keyword,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String fileType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultDTO> results = elasticsearchService.searchAttachments(keyword, courseId, fileType, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", results);

        return ResponseEntity.ok(response);
    }
}
