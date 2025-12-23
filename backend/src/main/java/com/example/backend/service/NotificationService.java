package com.example.backend.service;

import com.example.backend.config.RabbitMQConfig;
import com.example.backend.dto.NotificationDTO;
import com.example.backend.dto.NotificationMessage;
import com.example.backend.entity.Notification;
import com.example.backend.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 发送通知（异步通过MQ）
     */
    public void sendNotification(Long userId, String title, String content, String type, Long relatedId) {
        NotificationMessage message = new NotificationMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.NOTIFICATION_EXCHANGE,
            RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
            message
        );
    }

    /**
     * MQ消费者 - 处理通知
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationMessage message) {
        // 1. 保存到数据库
        Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        notification.setTitle(message.getTitle());
        notification.setContent(message.getContent());
        notification.setType(message.getType());
        notification.setRelatedId(message.getRelatedId());
        notificationRepository.save(notification);

        // 2. 通过WebSocket推送给用户
        NotificationDTO dto = NotificationDTO.fromEntity(notification);
        messagingTemplate.convertAndSendToUser(
            message.getUserId().toString(),
            "/queue/notifications",
            dto
        );
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public Page<NotificationDTO> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDTO::fromEntity);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
    }
}
