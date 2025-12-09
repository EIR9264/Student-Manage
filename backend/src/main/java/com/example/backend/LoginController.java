// backend/src/main/java/com/example/backend/LoginController.java
package com.example.backend;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    // 这是一个模拟登录，实际开发会查数据库
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        Map<String, Object> response = new HashMap<>();

        // 假设账号是 admin，密码是 123456
        if ("admin".equals(username) && "123456".equals(password)) {
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", "fake-jwt-token-123"); // 模拟Token
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        return response;
    }
}