package com.example.backend.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.attachment}")
    private String attachmentBucket;

    @Value("${minio.bucket.avatar:avatars}")
    private String avatarBucket;

    /**
     * 确保bucket存在
     */
    public void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("创建bucket失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件
     * @return 存储路径
     */
    public String uploadFile(MultipartFile file, Long courseId) {
        try {
            ensureBucketExists(attachmentBucket);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成存储路径: courseId/uuid.ext
            String objectName = courseId + "/" + UUID.randomUUID().toString() + extension;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(attachmentBucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(attachmentBucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取预签名URL（用于预览/下载）
     * @param expiry 过期时间（分钟）
     */
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(attachmentBucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("获取预签名URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(attachmentBucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件流（用于内容提取）
     */
    public InputStream getFileStream(String objectName) {
        return downloadFile(objectName);
    }

    public String getAttachmentBucket() {
        return attachmentBucket;
    }

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            ensureBucketExists(avatarBucket);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只能上传图片文件");
            }

            // 生成存储路径: userId/uuid.ext
            String objectName = userId + "/" + UUID.randomUUID().toString() + extension;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(avatarBucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取头像URL
     */
    public String getAvatarUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(avatarBucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            return null;
        }
    }
}
