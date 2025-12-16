package com.example.backend.config;

import com.example.backend.interceptor.AuthInterceptor;
import com.example.backend.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器：拦截所有API，排除登录、注册和验证码
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/register", "/api/captcha", "/api/check-username");

        // 权限拦截器：拦截需要管理员权限的API
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/students/**")
                .addPathPatterns("/api/courses/**")
                .addPathPatterns("/api/attachments/**")
                .excludePathPatterns("/api/enrollments/**"); // 选课接口学生可用
    }
}
