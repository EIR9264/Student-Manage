package com.example.backend.aspect;

import com.example.backend.annotation.DataScope;
import com.example.backend.context.DataScopeContext;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Aspect
@Component
@Order(2)
public class DataScopeAspect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint point, DataScope dataScope) {
        ServletRequestAttributes attrs = (ServletRequestAttributes)
            RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        List<Role> roles = roleRepository.findByUserId(userId);
        DataScopeContext.DataScopeInfo info = new DataScopeContext.DataScopeInfo();
        info.setUserId(userId);
        info.setDepartmentId(user.getDepartmentId());

        String maxScope = "SELF";
        Set<Long> customDeptIds = new HashSet<>();

        for (Role role : roles) {
            String scope = role.getDataScope();
            if ("ALL".equals(scope)) {
                maxScope = "ALL";
                break;
            } else if ("DEPT_TREE".equals(scope) && !"ALL".equals(maxScope)) {
                maxScope = "DEPT_TREE";
            } else if ("DEPT".equals(scope) && "SELF".equals(maxScope)) {
                maxScope = "DEPT";
            }
        }

        info.setDataScope(maxScope);
        info.setCustomDeptIds(customDeptIds);
        DataScopeContext.set(info);
    }

    @After("@annotation(dataScope)")
    public void doAfter(JoinPoint point, DataScope dataScope) {
        DataScopeContext.clear();
    }
}
