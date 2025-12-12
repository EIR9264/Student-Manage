package com.example.backend;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.RegisterRequest;
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

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

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
    public LoginResponse login(@RequestBody LoginRequest request) {
        LoginResponse response = new LoginResponse();

        // 1. 校验验证码
        if (request.getCaptchaKey() == null || request.getCaptchaCode() == null) {
            response.setSuccess(false);
            response.setMessage("请输入验证码");
            return response;
        }

        String realCode = redisTemplate.opsForValue().get("captcha:" + request.getCaptchaKey());
        if (realCode == null) {
            response.setSuccess(false);
            response.setMessage("验证码已过期，请刷新");
            return response;
        }

        if (!realCode.equalsIgnoreCase(request.getCaptchaCode())) {
            response.setSuccess(false);
            response.setMessage("验证码错误");
            return response;
        }

        // 验证通过后，删除 Redis 中的验证码 (防止重复使用)
        redisTemplate.delete("captcha:" + request.getCaptchaKey());

        // 2. 用户认证
        try {
            User user = userService.authenticate(request.getUsername(), request.getPassword());
            String token = jwtUtil.generateToken(user);

            response.setSuccess(true);
            response.setMessage("登录成功");
            response.setToken(token);
            response.setUserInfo(userService.toUserInfo(user));
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    // 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 1. 校验验证码
        if (request.getCaptchaKey() == null || request.getCaptchaCode() == null) {
            response.put("success", false);
            response.put("message", "请输入验证码");
            return response;
        }

        String realCode = redisTemplate.opsForValue().get("captcha:" + request.getCaptchaKey());
        if (realCode == null) {
            response.put("success", false);
            response.put("message", "验证码已过期，请刷新");
            return response;
        }

        if (!realCode.equalsIgnoreCase(request.getCaptchaCode())) {
            response.put("success", false);
            response.put("message", "验证码错误");
            return response;
        }

        // 验证通过后，删除验证码
        redisTemplate.delete("captcha:" + request.getCaptchaKey());

        // 2. 用户注册
        try {
            userService.register(request);
            response.put("success", true);
            response.put("message", "注册成功，请登录");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 检查用户名是否存在
    @GetMapping("/check-username")
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.checkUsernameExists(username);
        response.put("exists", exists);
        return response;
    }
}