// backend/src/main/java/com/example/backend/Todo.java
package com.example.backend;

import jakarta.persistence.*; // 如果报错，可能是javax.persistence，取决于SpringBoot版本
// Spring Boot 3+ 使用 jakarta

@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private boolean completed;

    // Getters and Setters (必须有)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}