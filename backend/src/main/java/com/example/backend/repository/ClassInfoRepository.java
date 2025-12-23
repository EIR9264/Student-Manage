package com.example.backend.repository;

import com.example.backend.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
    Optional<ClassInfo> findByName(String name);
    boolean existsByName(String name);
}
