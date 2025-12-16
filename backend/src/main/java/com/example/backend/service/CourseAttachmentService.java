package com.example.backend.service;

import com.example.backend.dto.AttachmentDTO;
import com.example.backend.entity.CourseAttachment;
import com.example.backend.repository.CourseAttachmentRepository;
import com.example.backend.repository.CourseRepository;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseAttachmentService {

    @Autowired
    private CourseAttachmentRepository attachmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private final Tika tika = new Tika();

    /**
     * 上传附件
     */
    @Transactional
    public AttachmentDTO uploadAttachment(Long courseId, MultipartFile file, Integer sortOrder, Long createdBy) {
        // 检查课程是否存在
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("课程不存在");
        }

        // 上传到MinIO
        String storagePath = minioService.uploadFile(file, courseId);

        // 检测文件类型
        String fileType = detectFileType(file.getOriginalFilename(), file.getContentType());

        // 保存附件记录
        CourseAttachment attachment = new CourseAttachment();
        attachment.setCourseId(courseId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(fileType);
        attachment.setFileSize(file.getSize());
        attachment.setMimeType(file.getContentType());
        attachment.setStoragePath(storagePath);
        attachment.setBucketName(minioService.getAttachmentBucket());
        attachment.setSortOrder(sortOrder != null ? sortOrder : 0);
        attachment.setCreatedBy(createdBy);

        attachment = attachmentRepository.save(attachment);

        // 异步索引到ES
        elasticsearchService.indexAttachmentAsync(attachment);

        return AttachmentDTO.fromEntity(attachment);
    }

    /**
     * 获取课程附件列表
     */
    public List<AttachmentDTO> getAttachmentsByCourse(Long courseId) {
        List<CourseAttachment> attachments = attachmentRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        return attachments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取附件详情
     */
    public AttachmentDTO getAttachment(Long attachmentId) {
        CourseAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));
        return toDTO(attachment);
    }

    /**
     * 获取预览URL
     */
    public String getPreviewUrl(Long attachmentId) {
        CourseAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));
        return minioService.getPresignedUrl(attachment.getStoragePath(), 30); // 30分钟有效
    }

    /**
     * 获取下载URL并增加下载次数
     */
    @Transactional
    public String getDownloadUrl(Long attachmentId) {
        CourseAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));

        // 增加下载次数
        attachmentRepository.incrementDownloadCount(attachmentId);

        return minioService.getPresignedUrl(attachment.getStoragePath(), 5); // 5分钟有效
    }

    /**
     * 删除附件
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        CourseAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));

        // 删除MinIO文件
        try {
            minioService.deleteFile(attachment.getStoragePath());
        } catch (Exception e) {
            // 文件删除失败不影响数据库记录删除
        }

        // 删除ES索引
        elasticsearchService.deleteAttachmentIndex(attachmentId);

        // 删除数据库记录
        attachmentRepository.deleteById(attachmentId);
    }

    /**
     * 检测文件类型
     */
    private String detectFileType(String fileName, String mimeType) {
        if (fileName == null) return "OTHER";

        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) return "PDF";
        if (lowerName.endsWith(".doc")) return "DOC";
        if (lowerName.endsWith(".docx")) return "DOCX";
        if (lowerName.endsWith(".ppt")) return "PPT";
        if (lowerName.endsWith(".pptx")) return "PPTX";
        if (lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) return "EXCEL";
        if (lowerName.endsWith(".mp4") || lowerName.endsWith(".avi") ||
            lowerName.endsWith(".mov") || lowerName.endsWith(".mkv")) return "VIDEO";
        if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") ||
            lowerName.endsWith(".flac")) return "AUDIO";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
            lowerName.endsWith(".png") || lowerName.endsWith(".gif")) return "IMAGE";

        return "OTHER";
    }

    private AttachmentDTO toDTO(CourseAttachment attachment) {
        AttachmentDTO dto = AttachmentDTO.fromEntity(attachment);
        // 不在列表中生成URL，需要时单独获取
        return dto;
    }
}
