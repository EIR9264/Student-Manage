package com.example.backend.service;

import com.example.backend.dto.StudentDTO;
import com.example.backend.entity.ClassInfo;
import com.example.backend.entity.Student;
import com.example.backend.entity.User;
import com.example.backend.repository.ClassInfoRepository;
import com.example.backend.repository.StudentRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassInfoRepository classInfoRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private MinioService minioService;

    // 创建学生（同时创建User账号）
    @Transactional
    public Student createStudent(Student student) {
        // 验证班级必须存在
        if (student.getClassName() == null || student.getClassName().trim().isEmpty()) {
            throw new RuntimeException("请选择班级");
        }

        ClassInfo classInfo = classInfoRepository.findByName(student.getClassName())
                .orElseThrow(() -> new RuntimeException("班级不存在，请先创建班级"));

        student.setClassId(classInfo.getId());

        // 保存学生信息
        Student savedStudent = studentRepository.save(student);

        // 创建对应的User账号（默认密码123）
        User user = new User();
        user.setUsername(student.getStudentNumber()); // 用户名为学号
        user.setPassword(passwordUtil.encode("123")); // 默认密码123
        user.setRealName(student.getName());
        user.setRole("USER");
        user.setStudentId(savedStudent.getId());
        user.setStatus(1);
        userRepository.save(user);

        return savedStudent;
    }

    // 更新学生信息
    @Transactional
    public Student updateStudent(Long id, Student studentDetails, String newPassword, String newRole, String newEmail, String newPhone) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 检查是否是超级管理员 admin
        final boolean isAdminUser = userRepository.findByStudentId(id)
                .map(user -> "admin".equals(user.getUsername()))
                .orElse(false);

        // admin 的名字不能被修改
        if (!isAdminUser) {
            student.setName(studentDetails.getName());
        }
        student.setStudentNumber(studentDetails.getStudentNumber());
        student.setAge(studentDetails.getAge());
        student.setGender(studentDetails.getGender());
        student.setClassName(studentDetails.getClassName());

        // 通过 studentId 关联更新用户信息
        userRepository.findByStudentId(id).ifPresent(user -> {
            // 如果提供了新密码
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPassword(passwordUtil.encode(newPassword));
            }
            // 如果提供了新角色（超级管理员 admin 的角色不能被修改）
            if (newRole != null && !newRole.trim().isEmpty() && !isAdminUser) {
                user.setRole(newRole);
            }
            // 更新邮箱
            if (newEmail != null) {
                user.setEmail(newEmail.trim().isEmpty() ? null : newEmail);
            }
            // 更新手机号
            if (newPhone != null) {
                user.setPhone(newPhone.trim().isEmpty() ? null : newPhone);
            }
            userRepository.save(user);
        });

        return studentRepository.save(student);
    }

    // 删除学生（级联删除User账号）
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 检查是否是超级管理员 admin，禁止删除
        userRepository.findByStudentId(id).ifPresent(user -> {
            if ("admin".equals(user.getUsername())) {
                throw new RuntimeException("不能删除超级管理员");
            }
        });

        // 删除对应的User账号（通过 studentId 关联）
        userRepository.findByStudentId(id).ifPresent(userRepository::delete);

        // 删除学生
        studentRepository.deleteById(id);
    }

    // 转换为StudentDTO（根据角色决定是否包含密码）
    public StudentDTO toStudentDTO(Student student, boolean includePassword) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setStudentNumber(student.getStudentNumber());
        dto.setGender(student.getGender());
        dto.setAge(student.getAge());
        dto.setClassName(student.getClassName());

        // 设置头像信息
        dto.setAvatar(student.getAvatar());
        if (student.getAvatar() != null && !student.getAvatar().isEmpty()) {
            dto.setAvatarUrl(minioService.getAvatarUrl(student.getAvatar()));
        }

        // 通过 studentId 关联查询用户信息
        userRepository.findByStudentId(student.getId()).ifPresent(user -> {
            dto.setRole(user.getRole());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            // 如果需要包含密码（管理员）
            if (includePassword) {
                dto.setPassword("123"); // 显示默认密码提示，实际密码已加密
            }
        });

        return dto;
    }

    // 批量转换
    public List<StudentDTO> toStudentDTOList(List<Student> students, boolean includePassword) {
        List<StudentDTO> dtoList = new ArrayList<>();
        for (Student student : students) {
            dtoList.add(toStudentDTO(student, includePassword));
        }
        return dtoList;
    }

    // 获取所有班级列表
    public List<String> getAllClasses() {
        return studentRepository.findAll().stream()
                .map(Student::getClassName)
                .filter(className -> className != null && !className.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    // 更新学生头像
    @Transactional
    public void updateAvatar(Long studentId, String avatarPath) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        student.setAvatar(avatarPath);
        studentRepository.save(student);
    }

    // 获取学生头像路径
    public String getAvatarPath(Long studentId) {
        return studentRepository.findById(studentId)
                .map(Student::getAvatar)
                .orElse(null);
    }
}
