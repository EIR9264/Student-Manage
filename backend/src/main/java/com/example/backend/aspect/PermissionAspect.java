package com.example.backend.aspect;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
@Order(1)
public class PermissionAspect {

    @Autowired
    private PermissionService permissionService;

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint point, RequirePermission requirePermission) throws AccessDeniedException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AccessDeniedException("无法获取请求上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            throw new AccessDeniedException("未登录");
        }

        String[] requiredPermissions = requirePermission.value();
        RequirePermission.Logical logical = requirePermission.logical();

        Set<String> userPermissions = permissionService.getUserPermissionCodes(userId);

        boolean hasPermission;
        if (logical == RequirePermission.Logical.AND) {
            hasPermission = userPermissions.containsAll(Arrays.asList(requiredPermissions));
        } else {
            hasPermission = Arrays.stream(requiredPermissions).anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            throw new AccessDeniedException("权限不足");
        }
    }
}
