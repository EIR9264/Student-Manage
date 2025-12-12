package com.example.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成密码：123
        String password = "123";
        String encoded = encoder.encode(password);

        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encoded);
        System.out.println();

        // 验证密码
        boolean matches = encoder.matches(password, encoded);
        System.out.println("密码验证结果: " + matches);

        // 生成多个密码供选择
        System.out.println("\n生成3个不同的加密结果（都对应密码123）：");
        for (int i = 1; i <= 3; i++) {
            System.out.println(i + ". " + encoder.encode(password));
        }
    }
}
