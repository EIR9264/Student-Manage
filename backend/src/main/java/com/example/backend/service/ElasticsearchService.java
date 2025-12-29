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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ElasticsearchService {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchService.class);

    @Autowired
    private CourseAttachmentEsRepository esRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DocumentExtractorService documentExtractorService;

    @Autowired
    private com.example.backend.repository.CourseAttachmentRepository attachmentRepository;

    /**
     * 异步索引附件内容到ES
     */
    @Async("taskExecutor")
    public void indexAttachmentAsync(CourseAttachment attachment) {
        try {
            indexAttachment(attachment);
            // 更新索引状态
            attachment.setEsIndexed(true);
            attachment.setEsIndexTime(LocalDateTime.now());
            attachmentRepository.save(attachment);
            log.info("ES索引成功: {}", attachment.getFileName());
        } catch (Exception e) {
            log.error("ES索引失败: {}", e.getMessage());
        }
    }

    /**
     * 索引单个附件到ES
     */
    public void indexAttachment(CourseAttachment attachment) throws Exception {
        String courseName = courseRepository.findById(attachment.getCourseId())
                .map(Course::getCourseName)
                .orElse("");

        String content = "";
        String fileType = attachment.getFileType().toUpperCase();
        if (List.of("PDF", "DOC", "DOCX", "PPT", "PPTX").contains(fileType)) {
            try (InputStream inputStream = minioService.getFileStream(attachment.getStoragePath())) {
                content = documentExtractorService.extractText(inputStream, fileType);
            }
        }

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
     * 全文搜索附件 - 优先ES，失败时回退到数据库
     */
    public Page<SearchResultDTO> searchAttachments(String keyword, Long courseId, String fileType, Pageable pageable) {
        try {
            // 优先使用ES搜索
            Page<CourseAttachmentDocument> results;
            if (courseId != null) {
                results = esRepository.searchByCourseIdAndKeyword(courseId, keyword, pageable);
            } else {
                results = esRepository.searchByKeyword(keyword, pageable);
            }

            if (results.getTotalElements() > 0) {
                return convertEsResults(results, keyword, fileType, pageable);
            }
        } catch (Exception e) {
            log.warn("ES搜索失败，回退到数据库: {}", e.getMessage());
        }

        // ES无结果或失败，使用数据库搜索
        return searchFromDatabase(keyword, courseId, fileType, pageable);
    }

    private Page<SearchResultDTO> convertEsResults(Page<CourseAttachmentDocument> results, String keyword, String fileType, Pageable pageable) {
        List<SearchResultDTO> dtoList = new ArrayList<>();
        for (CourseAttachmentDocument doc : results.getContent()) {
            if (fileType != null && !fileType.isEmpty() && !fileType.equalsIgnoreCase(doc.getFileType())) {
                continue;
            }
            SearchResultDTO dto = new SearchResultDTO();
            dto.setAttachmentId(doc.getAttachmentId());
            dto.setCourseId(doc.getCourseId());
            dto.setCourseName(doc.getCourseName());
            dto.setFileName(doc.getFileName());
            dto.setFileType(doc.getFileType());

            // 生成高亮：优先内容，其次文件名
            dto.setHighlight(generateHighlight(doc.getContent(), doc.getFileName(), keyword));
            dtoList.add(dto);
        }
        return new PageImpl<>(dtoList, pageable, results.getTotalElements());
    }

    private String generateHighlight(String content, String fileName, String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        // 优先在内容中查找高亮
        if (content != null && !content.isEmpty()) {
            int index = content.toLowerCase().indexOf(lowerKeyword);
            if (index >= 0) {
                int start = Math.max(0, index - 50);
                int end = Math.min(content.length(), index + keyword.length() + 50);
                String snippet = content.substring(start, end);
                return "..." + highlightKeyword(snippet, keyword) + "...";
            }
        }

        // 在文件名中查找高亮
        if (fileName != null && fileName.toLowerCase().contains(lowerKeyword)) {
            return "文件名匹配: " + highlightKeyword(fileName, keyword);
        }

        return null;
    }

    private String highlightKeyword(String text, String keyword) {
        // 不区分大小写替换
        String regex = "(?i)" + java.util.regex.Pattern.quote(keyword);
        return text.replaceAll(regex, "<em>" + keyword + "</em>");
    }

    private Page<SearchResultDTO> searchFromDatabase(String keyword, Long courseId, String fileType, Pageable pageable) {
        List<CourseAttachment> attachments = courseId != null
                ? attachmentRepository.searchByCourseIdAndFileName(courseId, keyword)
                : attachmentRepository.searchByFileName(keyword);

        List<SearchResultDTO> dtoList = new ArrayList<>();
        for (CourseAttachment att : attachments) {
            if (fileType != null && !fileType.isEmpty() && !fileType.equalsIgnoreCase(att.getFileType())) {
                continue;
            }
            String courseName = courseRepository.findById(att.getCourseId()).map(Course::getCourseName).orElse("");
            SearchResultDTO dto = new SearchResultDTO();
            dto.setAttachmentId(att.getId());
            dto.setCourseId(att.getCourseId());
            dto.setCourseName(courseName);
            dto.setFileName(att.getFileName());
            dto.setFileType(att.getFileType());
            // 文件名高亮
            dto.setHighlight("文件名匹配: " + highlightKeyword(att.getFileName(), keyword));
            dtoList.add(dto);
        }
        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }

    /**
     * 重新索引所有未索引的附件
     */
    public int reindexAllAttachments() {
        List<CourseAttachment> unindexed = attachmentRepository.findByEsIndexedFalse();
        int successCount = 0;
        for (CourseAttachment attachment : unindexed) {
            try {
                indexAttachment(attachment);
                attachment.setEsIndexed(true);
                attachment.setEsIndexTime(LocalDateTime.now());
                attachmentRepository.save(attachment);
                successCount++;
                log.info("重新索引成功: {}", attachment.getFileName());
            } catch (Exception e) {
                log.error("重新索引失败 {}: {}", attachment.getFileName(), e.getMessage());
            }
        }
        return successCount;
    }

    /**
     * 强制重新索引所有附件
     */
    public int forceReindexAll() {
        List<CourseAttachment> all = attachmentRepository.findAll();
        int successCount = 0;
        for (CourseAttachment attachment : all) {
            try {
                indexAttachment(attachment);
                attachment.setEsIndexed(true);
                attachment.setEsIndexTime(LocalDateTime.now());
                attachmentRepository.save(attachment);
                successCount++;
            } catch (Exception e) {
                log.error("强制重新索引失败 {}: {}", attachment.getFileName(), e.getMessage());
            }
        }
        return successCount;
    }
}
