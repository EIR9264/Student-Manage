package com.example.backend.service;

import com.example.backend.dto.SearchResultDTO;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseAttachment;
import com.example.backend.es.CourseAttachmentDocument;
import com.example.backend.es.CourseAttachmentEsRepository;
import com.example.backend.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ElasticsearchService {

    @Autowired
    private CourseAttachmentEsRepository esRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DocumentExtractorService documentExtractorService;

    /**
     * 异步索引附件内容到ES
     */
    @Async("taskExecutor")
    public void indexAttachmentAsync(CourseAttachment attachment) {
        try {
            // 获取课程名称
            String courseName = courseRepository.findById(attachment.getCourseId())
                    .map(Course::getCourseName)
                    .orElse("");

            // 只对文档类型提取内容
            String content = "";
            String fileType = attachment.getFileType().toUpperCase();
            if (List.of("PDF", "DOC", "DOCX", "PPT", "PPTX").contains(fileType)) {
                try (InputStream inputStream = minioService.getFileStream(attachment.getStoragePath())) {
                    content = documentExtractorService.extractText(inputStream, fileType);
                }
            }

            // 创建ES文档
            CourseAttachmentDocument document = new CourseAttachmentDocument();
            document.setId(attachment.getId().toString());
            document.setAttachmentId(attachment.getId());
            document.setCourseId(attachment.getCourseId());
            document.setCourseName(courseName);
            document.setFileName(attachment.getFileName());
            document.setContent(content);
            document.setFileType(fileType);
            document.setCreatedAt(LocalDateTime.now());

            esRepository.save(document);
        } catch (Exception e) {
            // 索引失败不影响主流程，记录日志即可
            System.err.println("ES索引失败: " + e.getMessage());
        }
    }

    /**
     * 删除ES中的附件索引
     */
    public void deleteAttachmentIndex(Long attachmentId) {
        try {
            esRepository.deleteByAttachmentId(attachmentId);
        } catch (Exception e) {
            System.err.println("删除ES索引失败: " + e.getMessage());
        }
    }

    /**
     * 全文搜索附件
     */
    public Page<SearchResultDTO> searchAttachments(String keyword, Long courseId, String fileType, Pageable pageable) {
        try {
            Page<CourseAttachmentDocument> results;

            if (courseId != null) {
                results = esRepository.searchByCourseIdAndKeyword(courseId, keyword, pageable);
            } else {
                results = esRepository.searchByKeyword(keyword, pageable);
            }

            List<SearchResultDTO> dtoList = new ArrayList<>();
            for (CourseAttachmentDocument doc : results.getContent()) {
                // 如果指定了文件类型，过滤
                if (fileType != null && !fileType.isEmpty() && !fileType.equalsIgnoreCase(doc.getFileType())) {
                    continue;
                }

                SearchResultDTO dto = new SearchResultDTO();
                dto.setAttachmentId(doc.getAttachmentId());
                dto.setCourseId(doc.getCourseId());
                dto.setCourseName(doc.getCourseName());
                dto.setFileName(doc.getFileName());
                dto.setFileType(doc.getFileType());

                // 生成高亮片段
                String content = doc.getContent();
                if (content != null && !content.isEmpty()) {
                    int index = content.toLowerCase().indexOf(keyword.toLowerCase());
                    if (index >= 0) {
                        int start = Math.max(0, index - 50);
                        int end = Math.min(content.length(), index + keyword.length() + 50);
                        String highlight = "..." + content.substring(start, end)
                                .replace(keyword, "<em>" + keyword + "</em>") + "...";
                        dto.setHighlight(highlight);
                    }
                }

                dtoList.add(dto);
            }

            return new PageImpl<>(dtoList, pageable, results.getTotalElements());
        } catch (Exception e) {
            // ES不可用时返回空结果
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
}
