package com.example.backend.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseAttachmentEsRepository extends ElasticsearchRepository<CourseAttachmentDocument, String> {

    // 按课程ID查询
    List<CourseAttachmentDocument> findByCourseId(Long courseId);

    // 按文件类型查询
    List<CourseAttachmentDocument> findByFileType(String fileType);

    // 按附件ID删除
    void deleteByAttachmentId(Long attachmentId);

    // 全文搜索（文件名和内容）
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"fileName\", \"content\", \"courseName\"]}}]}}")
    Page<CourseAttachmentDocument> searchByKeyword(String keyword, Pageable pageable);

    // 按课程ID和关键词搜索
    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}, {\"multi_match\": {\"query\": \"?1\", \"fields\": [\"fileName\", \"content\"]}}]}}")
    Page<CourseAttachmentDocument> searchByCourseIdAndKeyword(Long courseId, String keyword, Pageable pageable);
}
