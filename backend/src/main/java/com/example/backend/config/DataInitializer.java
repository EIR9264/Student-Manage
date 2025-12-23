package com.example.backend.config;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initPermissions();
        assignAdminRole();
        assignAdminPermissions();
    }

    private void initRoles() {
        if (roleRepository.findByCode("ADMIN") == null) {
            Role admin = new Role();
            admin.setCode("ADMIN");
            admin.setName("管理员");
            admin.setDescription("系统管理员，拥有所有权限");
            admin.setDataScope("ALL");
            roleRepository.save(admin);
        }

        if (roleRepository.findByCode("USER") == null) {
            Role user = new Role();
            user.setCode("USER");
            user.setName("普通用户");
            user.setDescription("普通用户，拥有基本权限");
            user.setDataScope("SELF");
            roleRepository.save(user);
        }
    }

    private void initPermissions() {
        // 初始化基础权限码
        createPermissionIfNotExists("student:view", "查看学生", "API");
        createPermissionIfNotExists("student:create", "创建学生", "API");
        createPermissionIfNotExists("student:update", "更新学生", "API");
        createPermissionIfNotExists("student:delete", "删除学生", "API");

        createPermissionIfNotExists("course:view", "查看课程", "API");
        createPermissionIfNotExists("course:create", "创建课程", "API");
        createPermissionIfNotExists("course:update", "更新课程", "API");
        createPermissionIfNotExists("course:delete", "删除课程", "API");

        createPermissionIfNotExists("enrollment:view", "查看选课", "API");
        createPermissionIfNotExists("enrollment:enroll", "选课", "API");
        createPermissionIfNotExists("enrollment:drop", "退课", "API");

        createPermissionIfNotExists("user:view", "查看用户", "API");
        createPermissionIfNotExists("user:manage", "管理用户", "API");
    }

    private void createPermissionIfNotExists(String code, String name, String type) {
        if (permissionRepository.findByCode(code).isEmpty()) {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            permission.setType(type);
            permissionRepository.save(permission);
        }
    }

    private void assignAdminRole() {
        // 为 admin 用户分配管理员角色
        userRepository.findByUsername("admin").ifPresent(admin -> {
            Role adminRole = roleRepository.findByCode("ADMIN");
            if (adminRole != null) {
                boolean hasRole = userRoleRepository.findByUserId(admin.getId())
                        .stream()
                        .anyMatch(ur -> ur.getRoleId().equals(adminRole.getId()));

                if (!hasRole) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(admin.getId());
                    userRole.setRoleId(adminRole.getId());
                    userRoleRepository.save(userRole);
                }
            }
        });
    }

    private void assignAdminPermissions() {
        // 为管理员角色分配所有权限
        Role adminRole = roleRepository.findByCode("ADMIN");
        if (adminRole == null) return;

        var existingPerms = rolePermissionRepository.findByRoleId(adminRole.getId());
        var allPermissions = permissionRepository.findAll();

        for (Permission perm : allPermissions) {
            boolean hasPermission = existingPerms.stream()
                    .anyMatch(rp -> rp.getPermissionId().equals(perm.getId()));
            if (!hasPermission) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(adminRole.getId());
                rp.setPermissionId(perm.getId());
                rolePermissionRepository.save(rp);
            }
        }
    }
}
