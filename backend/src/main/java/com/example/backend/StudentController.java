package com.example.backend;

import com.example.backend.dto.StudentDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    // 获取列表：支持 ?keyword=xxx&className=xxx 查询
    @GetMapping
    public List<StudentDTO> getAllStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String className,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        boolean isAdmin = "ADMIN".equals(role);

        List<Student> students;

        // 如果指定了班级
        if (className != null && !className.trim().isEmpty()) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 班级 + 关键词搜索
                students = studentRepository.findByClassNameAndNameContainingOrClassNameAndStudentNumberContaining(
                    className, keyword, className, keyword);
            } else {
                // 只按班级查询
                students = studentRepository.findByClassName(className);
            }
        } else {
            // 原有逻辑：关键词搜索或查所有
            if (keyword != null && !keyword.trim().isEmpty()) {
                students = studentRepository.findByNameContainingOrStudentNumberContaining(keyword, keyword);
            } else {
                students = studentRepository.findAll();
            }
        }

        // 根据角色返回不同的DTO（管理员可以看到密码字段）
        return studentService.toStudentDTOList(students, isAdmin);
    }

    @PostMapping
    public Map<String, Object> createStudent(@RequestBody Student student) {
        Map<String, Object> result = new HashMap<>();
        try {
            Student savedStudent = studentService.createStudent(student);
            result.put("success", true);
            result.put("message", "创建成功");
            result.put("data", savedStudent);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateStudent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Student studentDetails = new Student();
            studentDetails.setName((String) requestData.get("name"));
            studentDetails.setStudentNumber((String) requestData.get("studentNumber"));
            studentDetails.setGender((String) requestData.get("gender"));
            studentDetails.setAge((Integer) requestData.get("age"));
            studentDetails.setClassName((String) requestData.get("className"));

            String newPassword = (String) requestData.get("password");

            Student updatedStudent = studentService.updateStudent(id, studentDetails, newPassword);
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("data", updatedStudent);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteStudent(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            studentService.deleteStudent(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // 获取所有班级列表（用于前端下拉选择）
    @GetMapping("/classes")
    public List<String> getAllClasses() {
        return studentService.getAllClasses();
    }
}