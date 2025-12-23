package com.example.backend.service;

import com.example.backend.entity.ClassInfo;
import com.example.backend.repository.ClassInfoRepository;
import com.example.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassInfoService {

    @Autowired
    private ClassInfoRepository classInfoRepository;

    @Autowired
    private StudentRepository studentRepository;

    public List<ClassInfo> getAllClasses() {
        return classInfoRepository.findAll();
    }

    public Optional<ClassInfo> getClassById(Long id) {
        return classInfoRepository.findById(id);
    }

    public Optional<ClassInfo> getClassByName(String name) {
        return classInfoRepository.findByName(name);
    }

    public ClassInfo createClass(String name) {
        if (classInfoRepository.existsByName(name)) {
            throw new RuntimeException("班级名称已存在");
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(name);
        return classInfoRepository.save(classInfo);
    }

    public ClassInfo updateClass(Long id, String name) {
        ClassInfo classInfo = classInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        if (hasStudents(classInfo.getName())) {
            throw new RuntimeException("班级内有学生，无法修改");
        }

        if (!classInfo.getName().equals(name) && classInfoRepository.existsByName(name)) {
            throw new RuntimeException("班级名称已存在");
        }

        classInfo.setName(name);
        return classInfoRepository.save(classInfo);
    }

    public void deleteClass(Long id) {
        ClassInfo classInfo = classInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        if (hasStudents(classInfo.getName())) {
            throw new RuntimeException("班级内有学生，无法删除");
        }

        classInfoRepository.deleteById(id);
    }

    public boolean hasStudents(String className) {
        return studentRepository.countByClassName(className) > 0;
    }

    public long getStudentCount(String className) {
        return studentRepository.countByClassName(className);
    }
}
