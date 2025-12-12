package com.example.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求（CORS预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String role = (String) request.getAttribute("role");
        String method = request.getMethod();

        // 只有管理员可以执行POST、PUT、DELETE操作
        if (!"ADMIN".equals(role) && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"权限不足，仅管理员可操作\"}");
            return false;
        }

        return true;
    }
}
