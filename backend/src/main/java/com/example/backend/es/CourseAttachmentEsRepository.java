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

    // 全文搜索（文件名和内容）- 使用 wildcard 支持中文
    @Query("{\"bool\": {\"should\": [{\"wildcard\": {\"fileName\": \"*?0*\"}}, {\"wildcard\": {\"content\": \"*?0*\"}}, {\"wildcard\": {\"courseName\": \"*?0*\"}}, {\"match\": {\"fileName\": \"?0\"}}, {\"match\": {\"content\": \"?0\"}}], \"minimum_should_match\": 1}}")
    Page<CourseAttachmentDocument> searchByKeyword(String keyword, Pageable pageable);

    // 按课程ID和关键词搜索
    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}], \"should\": [{\"wildcard\": {\"fileName\": \"*?1*\"}}, {\"wildcard\": {\"content\": \"*?1*\"}}, {\"match\": {\"fileName\": \"?1\"}}, {\"match\": {\"content\": \"?1\"}}], \"minimum_should_match\": 1}}")
    Page<CourseAttachmentDocument> searchByCourseIdAndKeyword(Long courseId, String keyword, Pageable pageable);
}
