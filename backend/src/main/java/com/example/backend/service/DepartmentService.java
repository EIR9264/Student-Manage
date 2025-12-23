package com.example.backend.service;

import com.example.backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 获取部门及所有子部门ID
     */
    public Set<Long> getDeptTreeIds(Long deptId) {
        Set<Long> result = new HashSet<>();
        result.add(deptId);
        collectChildIds(deptId, result);
        return result;
    }

    private void collectChildIds(Long parentId, Set<Long> result) {
        List<Long> childIds = departmentRepository.findChildIds(parentId);
        for (Long childId : childIds) {
            result.add(childId);
            collectChildIds(childId, result);
        }
    }
}
