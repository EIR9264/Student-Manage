package com.example.backend.service;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UpdatePasswordRequest;
import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.dto.UserInfo;
import com.example.backend.entity.Student;
import com.example.backend.entity.User;
import com.example.backend.repository.StudentRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    // 用户认证
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordUtil.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }

    // 用户注册
    @Transactional
    public User register(RegisterRequest request) {
        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 验证用户名唯一性
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 密码强度验证（至少6位）
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度至少为6位");
        }

        // 验证必填字段
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            throw new RuntimeException("性别不能为空");
        }

        // 1. 先创建Student记录
        Student student = new Student();
        student.setStudentNumber(request.getUsername()); // 学号就是用户名
        student.setName(request.getRealName());
        student.setGender(request.getGender());
        student.setAge(18); // 默认年龄
        student.setClassName("未分配"); // 默认班级
        Student savedStudent = studentRepository.save(student);

        // 2. 创建User记录，关联到Student
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordUtil.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStudentId(savedStudent.getId()); // 关联Student
        user.setStatus(1);

        return userRepository.save(user);
    }

    // 检查用户名是否存在
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // 获取用户信息
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 根据用户名获取用户
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 更新个人信息
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return userRepository.save(user);
    }

    // 修改密码
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("密码长度至少为6位");
        }

        User user = getUserById(userId);

        if (!passwordUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordUtil.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // 转换为UserInfo（不包含敏感信息）
    public UserInfo toUserInfo(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRealName(user.getRealName());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());
        info.setRole(user.getRole());
        info.setStudentId(user.getStudentId());
        return info;
    }

    // 获取所有用户列表
    public List<UserInfo> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserInfo)
                .collect(Collectors.toList());
    }

    // 修改用户角色（根据操作者权限判断）
    public void updateUserRole(Long currentUserId, Long targetUserId, String newRole) {
        User currentUser = getUserById(currentUserId);
        User targetUser = getUserById(targetUserId);

        // 1. 验证角色值
        if (!"ADMIN".equals(newRole) && !"USER".equals(newRole)) {
            throw new RuntimeException("无效的角色值，只能是ADMIN或USER");
        }

        // 2. 不能修改自己的角色
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("不能修改自己的角色");
        }

        // 3. 超级管理员admin不能被任何人修改
        if ("admin".equals(targetUser.getUsername())) {
            throw new RuntimeException("不能修改超级管理员admin的角色");
        }

        // 4. 普通管理员不能修改其他管理员的角色
        if (!"admin".equals(currentUser.getUsername()) && "ADMIN".equals(targetUser.getRole())) {
            throw new RuntimeException("普通管理员不能修改其他管理员的角色");
        }

        // 5. 执行角色修改
        targetUser.setRole(newRole);
        userRepository.save(targetUser);
    }
}
