package com.example.backend;

import com.example.backend.dto.StudentDTO;
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
    private PasswordUtil passwordUtil;

    // 创建学生（同时创建User账号）
    @Transactional
    public Student createStudent(Student student) {
        // 如果没有指定班级，设置默认值
        if (student.getClassName() == null || student.getClassName().trim().isEmpty()) {
            student.setClassName("未分配");
        }

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
    public Student updateStudent(Long id, Student studentDetails, String newPassword) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        student.setName(studentDetails.getName());
        student.setStudentNumber(studentDetails.getStudentNumber());
        student.setAge(studentDetails.getAge());
        student.setGender(studentDetails.getGender());
        student.setClassName(studentDetails.getClassName());

        // 如果提供了新密码，更新对应User的密码
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            userRepository.findByUsername(student.getStudentNumber()).ifPresent(user -> {
                user.setPassword(passwordUtil.encode(newPassword));
                userRepository.save(user);
            });
        }

        return studentRepository.save(student);
    }

    // 删除学生（级联删除User账号）
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 删除对应的User账号
        userRepository.findByUsername(student.getStudentNumber()).ifPresent(userRepository::delete);

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

        // 如果需要包含密码（管理员），获取对应User的密码
        if (includePassword) {
            userRepository.findByUsername(student.getStudentNumber()).ifPresent(user -> {
                dto.setPassword("123"); // 显示默认密码提示，实际密码已加密
            });
        }

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
}
