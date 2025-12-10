package com.example.backend;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/captcha")
    public Map<String, String> getCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String code = lineCaptcha.getCode();
        String base64Data = lineCaptcha.getImageBase64Data();

        String captchaKey = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("captcha:" + captchaKey, code, 5, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("image", base64Data);

        return result;
    }

    // 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");
        String captchaCode = user.get("captchaCode"); // 用户输入的验证码
        String captchaKey = user.get("captchaKey"); // 前端传回的 Key

        Map<String, Object> response = new HashMap<>();

        // 1. 校验验证码
        if (captchaKey == null || captchaCode == null) {
            response.put("success", false);
            response.put("message", "请输入验证码");
            return response;
        }

        String realCode = redisTemplate.opsForValue().get("captcha:" + captchaKey);
        if (realCode == null) {
            response.put("success", false);
            response.put("message", "验证码已过期，请刷新");
            return response;
        }

        if (!realCode.equalsIgnoreCase(captchaCode)) {
            response.put("success", false);
            response.put("message", "验证码错误");
            return response;
        }

        // 验证通过后，删除 Redis 中的验证码 (防止重复使用)
        redisTemplate.delete("captcha:" + captchaKey);

        // 2. 校验账号密码 (这里依然是模拟，实际应该查数据库)
        if ("admin".equals(username) && "123".equals(password)) {
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", "fake-jwt-token-" + UUID.randomUUID());
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        return response;
    }
}