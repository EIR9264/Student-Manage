package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    // 获取列表：支持 ?keyword=xxx 查询
    @GetMapping
    public List<Student> getAllStudents(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 如果传了 keyword，就按 姓名 或 学号 模糊查找
            // 两个参数都传 keyword，表示 "姓名包含keyword" OR "学号包含keyword"
            return studentRepository.findByNameContainingOrStudentNumberContaining(keyword, keyword);
        }
        // 没传 keyword，查所有
        return studentRepository.findAll();
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        Student student = studentRepository.findById(id).orElseThrow();
        student.setName(studentDetails.getName());
        student.setStudentNumber(studentDetails.getStudentNumber());
        student.setAge(studentDetails.getAge());
        student.setGender(studentDetails.getGender());
        return studentRepository.save(student);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
    }
}