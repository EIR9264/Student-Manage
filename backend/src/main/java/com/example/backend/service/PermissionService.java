package com.example.backend.service;

import com.example.backend.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PERMISSION_CACHE_KEY = "user:permissions:";

    /**
     * 获取用户权限码集合（带缓存）
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissionCodes(Long userId) {
        String cacheKey = PERMISSION_CACHE_KEY + userId;

        // 先从Redis缓存获取
        Set<String> permissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);

        if (permissions == null) {
            // 从数据库查询
            permissions = permissionRepository.findPermissionCodesByUserId(userId);
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            // 缓存30分钟
            redisTemplate.opsForValue().set(cacheKey, permissions, 30, TimeUnit.MINUTES);
        }

        return permissions;
    }

    /**
     * 清除用户权限缓存
     */
    public void clearUserPermissionCache(Long userId) {
        redisTemplate.delete(PERMISSION_CACHE_KEY + userId);
    }

    /**
     * 清除所有用户权限缓存
     */
    public void clearAllPermissionCache() {
        Set<String> keys = redisTemplate.keys(PERMISSION_CACHE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
