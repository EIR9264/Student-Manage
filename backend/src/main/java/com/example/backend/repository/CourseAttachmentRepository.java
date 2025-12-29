package com.example.backend.repository;

import com.example.backend.entity.CourseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseAttachmentRepository extends JpaRepository<CourseAttachment, Long> {

    List<CourseAttachment> findByCourseIdOrderBySortOrderAsc(Long courseId);

    List<CourseAttachment> findByCourseIdAndFileType(Long courseId, String fileType);

    // 查询未索引到ES的附件
    List<CourseAttachment> findByEsIndexedFalse();

    // 增加下载次数
    @Modifying
    @Query("UPDATE CourseAttachment a SET a.downloadCount = a.downloadCount + 1 WHERE a.id = :id")
    void incrementDownloadCount(@Param("id") Long id);

    // 删除课程的所有附件记录
    void deleteByCourseId(Long courseId);

    // 统计课程附件数量
    long countByCourseId(Long courseId);

    // 按文件名搜索
    @Query("SELECT a FROM CourseAttachment a WHERE a.fileName LIKE %:keyword%")
    List<CourseAttachment> searchByFileName(@Param("keyword") String keyword);

    // 按课程ID和文件名搜索
    @Query("SELECT a FROM CourseAttachment a WHERE a.courseId = :courseId AND a.fileName LIKE %:keyword%")
    List<CourseAttachment> searchByCourseIdAndFileName(@Param("courseId") Long courseId, @Param("keyword") String keyword);
}
