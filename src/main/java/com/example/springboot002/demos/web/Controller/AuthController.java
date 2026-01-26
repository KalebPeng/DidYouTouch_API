package com.example.springboot002.demos.web.Controller;

import com.example.springboot002.demos.web.DTO.Request.*;
import com.example.springboot002.demos.web.DTO.Request.AuthRequest.LoginRequest;
import com.example.springboot002.demos.web.DTO.Request.AuthRequest.RegisterRequest;
import com.example.springboot002.demos.web.DTO.Response.*;
import com.example.springboot002.demos.web.Entity.User;
import com.example.springboot002.demos.web.Entity.LoginSession;
import com.example.springboot002.demos.web.Service.*;
import com.example.springboot002.demos.web.Service.AuthService.AliSmsService;
import com.example.springboot002.demos.web.Service.AuthService.LoginSessionService;
import com.example.springboot002.demos.web.Service.AuthService.SmsCodeService;
import com.example.springboot002.demos.web.Util.JwtUtil;
import com.example.springboot002.demos.web.Util.PasswordUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "用户认证相关接口")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginSessionService loginSessionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private AliSmsService aliSmsService;

    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 检查邮箱是否已存在
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new Response("EMAIL_EXISTS", "该邮箱已被注册"));
            }

            // 检查手机号是否已存在（如果提供）
            if (request.getPhone() != null && userService.existsByPhone(request.getPhone())) {
                return ResponseEntity.badRequest()
                    .body(new Response("PHONE_EXISTS", "该手机号已被注册"));
            }

            // 创建新用户
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setPhone(request.getPhone());
            newUser.setNickname(request.getNickname());
            if (request.getGender() != null) {
                try {
                    newUser.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    newUser.setGender(User.Gender.unknown);
                }
            } else {
                newUser.setGender(User.Gender.unknown);
            }
            newUser.setBirthdate(request.getBirthdate());

            // 设置密码（使用工具类加密）
            String hashedPassword = passwordUtil.hashPassword(request.getPassword());
            newUser.setPasswordHash(hashedPassword);

            // 设置默认值
            newUser.setIsActive(true);
            newUser.setIsVerified(false);
            newUser.setMfaEnabled(false);
            newUser.setFailedLoginAttempts(0);
            newUser.setAccountType(User.AccountType.standard);
            newUser.setLanguage("zh-CN");
            newUser.setTimezone("Asia/Shanghai");
            newUser.setSalt(UUID.randomUUID().toString());

            User savedUser = userService.createUser(newUser);

            // 生成JWT令牌
            String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail());

            // 返回注册成功响应
            RegisterResponse response = new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                token,
                "注册成功"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("REGISTER_ERROR", "注册失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 查找用户
            User user = userService.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("INVALID_CREDENTIALS", "邮箱或密码错误"));
            }

            // 检查账户是否被锁定
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response("ACCOUNT_LOCKED", "账户已被锁定，请稍后再试"));
            }

            // 检查账户是否激活
            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response("ACCOUNT_INACTIVE", "账户未激活"));
            }

            // 验证密码
            if (!passwordUtil.checkPassword(request.getPassword(), user.getPasswordHash())) {
                // 增加失败登录次数
                userService.incrementFailedLoginAttempts(user.getId());

                // 检查是否需要锁定账户（失败5次锁定30分钟）
                if (user.getFailedLoginAttempts() >= 4) {
                    userService.lockAccount(user.getId(), 30);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Response("ACCOUNT_LOCKED", "登录失败次数过多，账户已被锁定30分钟"));
                }

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("INVALID_CREDENTIALS", "邮箱或密码错误"));
            }

            // 重置失败登录次数
            userService.resetFailedLoginAttempts(user.getId());

            // 更新最后登录时间
            userService.updateLastLogin(user.getId());

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            // 创建登录会话
            LoginSession session = loginSessionService.createSession(
                user,
                token,
                request.getDeviceId(),
                request.getDeviceType(),
                request.getDeviceName(),
                request.getDeviceModel()
            );

            // 返回登录成功响应
            LoginResponse response = new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                userService.isAdmin(user),
                token,
                session.getExpiresAt(),
                "登录成功"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("LOGIN_ERROR", "登录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从header中提取token
            String token = authHeader.replace("Bearer ", "");

            // 验证token
            String email = jwtUtil.extractEmail(token);
            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("INVALID_TOKEN", "无效的令牌"));
            }

            // 撤销会话
            loginSessionService.revokeSession(token);

            Map<String, String> response = new HashMap<>();
            response.put("message", "登出成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("LOGOUT_ERROR", "登出失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            // 从token中获取用户信息
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("INVALID_TOKEN", "无效的令牌"));
            }

            UUID userId = jwtUtil.extractUserId(token);
            User user = userService.findById(userId);

            // 验证旧密码
            if (!passwordUtil.checkPassword(request.getOldPassword(), user.getPasswordHash())) {
                return ResponseEntity.badRequest()
                    .body(new Response("INVALID_PASSWORD", "旧密码错误"));
            }

            // 更新密码
            String newHashedPassword = passwordUtil.hashPassword(request.getNewPassword());
            userService.updatePassword(user.getId(), newHashedPassword);

            // 撤销所有会话（安全考虑）
            loginSessionService.revokeAllUserSessions(user.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "密码修改成功，请重新登录");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("PASSWORD_CHANGE_ERROR", "密码修改失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String oldToken = authHeader.replace("Bearer ", "");

            // 验证旧token
            String email = jwtUtil.extractEmail(oldToken);
            if (!jwtUtil.validateToken(oldToken, email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("INVALID_TOKEN", "无效的令牌"));
            }

            // 提取用户信息
            UUID userId = jwtUtil.extractUserId(oldToken);

            // 生成新token
            String newToken = jwtUtil.generateToken(userId, email);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);
            response.put("message", "令牌刷新成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("TOKEN_REFRESH_ERROR", "令牌刷新失败: " + e.getMessage()));
        }
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-verify-code")
    public Map<String, Object> sendVerifyCode(@RequestParam String phoneNumber) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证手机号格式
            if (!isValidPhoneNumber(phoneNumber)) {
                result.put("success", false);
                result.put("message", "手机号格式不正确");
                return result;
            }

            // 2. 检查是否可以发送（防止频繁发送）
            if (!smsCodeService.canSend(phoneNumber)) {
                long remainingTime = smsCodeService.getRemainingSendTime(phoneNumber);
                result.put("success", false);
                result.put("message", "验证码发送过于频繁，请" + remainingTime + "秒后再试");
                result.put("remainingTime", remainingTime);
                return result;
            }

            // 3. 生成验证码
            String code = smsCodeService.generateCode();

            // 4. 发送短信
            boolean sent = aliSmsService.sendVerifyCode(phoneNumber, code);

            if (sent) {
                // 5. 保存验证码到 Redis
                smsCodeService.saveCode(phoneNumber, code);

                result.put("success", true);
                result.put("message", "验证码发送成功");
                result.put("expireTime", 300); // 5分钟
            } else {
                result.put("success", false);
                result.put("message", "验证码发送失败，请稍后重试");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误: " + e.getMessage());
        }

        return result;
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    public Map<String, Object> verifyCode(@RequestParam String phoneNumber,
                                          @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从 Redis 验证
            boolean isValid = smsCodeService.verifyCode(phoneNumber, code);

            if (isValid) {
                result.put("success", true);
                result.put("message", "验证成功");
            } else {
                int remainingTimes = smsCodeService.getRemainingRetryTimes(phoneNumber);
                result.put("success", false);
                if (remainingTimes > 0) {
                    result.put("message", "验证码错误，还可以尝试" + remainingTimes + "次");
                    result.put("remainingTimes", remainingTimes);
                } else {
                    result.put("message", "验证码错误或已过期");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/testEmail")
    public Map<String, Object> sendEmail(String email) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            if (!isValidEmail(email)) {
                result.put("success", false);
                result.put("message", "邮箱格式不正确");
                return result;
            }
            // 2. 检查是否可以发送（防止频繁发送）
            if (!smsCodeService.canSend(email)) {
                return replyCode(email);
            }
            //生成验证码
            String code = smsCodeService.generateCode();
            //发送邮件
            boolean b = emailService.sendSimpleEmail(email, code);
            //判断是否发送成功返回
            return tet(b,result,email,code);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误: " + e.getMessage());
        }

        return result;
    }
    /**
     * 验证码发送频繁回复
     */
    private Map<String,Object> replyCode(String email){
        long remainingTime = smsCodeService.getRemainingSendTime(email);
        HashMap<String, Object> hs = new HashMap<>();
        hs.put("success",false);
        hs.put("message","验证码发送过于频繁，请" + remainingTime + "秒后再试");
        hs.put("remainingTime", remainingTime);
        return hs;
    }
    /**
     * 判断是否发送成功栟保存验证码到redis
     */
    private Map<String,Object> tet(boolean b,Map<String,Object> result,String email,String code){
        if (b) {
            // 5. 保存验证码到 Redis
            smsCodeService.saveCode(email, code);

            result.put("success", true);
            result.put("message", "验证码发送成功");
            result.put("expireTime", 300);
        } else {
            result.put("success", false);
            result.put("message", "验证码发送失败，请稍后重试");
        }
        return result;
    }
    /**
     * 验证手机号格式
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
}