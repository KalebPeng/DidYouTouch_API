package com.example.springboot002.demos.web.Service;

import com.example.springboot002.demos.web.Entity.LoginSession;
import com.example.springboot002.demos.web.Entity.User;
import com.example.springboot002.demos.web.Repository.LoginSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LoginSessionService {

    @Autowired
    private LoginSessionRepository loginSessionRepository;

    @Value("${app.session.expiration:7200}")
    private int sessionExpirationSeconds;

    // 创建新会话
    public LoginSession createSession(User user, String token, String deviceId,
                                     String deviceType, String deviceName, String deviceModel) {
        LoginSession session = new LoginSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setSessionToken(token);
        session.setDeviceId(deviceId != null ? deviceId : UUID.randomUUID().toString());
        session.setDeviceType(deviceType != null ? deviceType : "UNKNOWN");
        session.setDeviceName(deviceName);
        session.setDeviceModel(deviceModel);

        // 获取IP地址
        String ipAddress = getClientIpAddress();
        session.setIpAddress(ipAddress);

        // 获取User Agent
        HttpServletRequest request = getCurrentRequest();
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        session.setUserAgent(userAgent);

        // 设置会话时间
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(sessionExpirationSeconds));
        session.setLastActivityAt(LocalDateTime.now());

        return loginSessionRepository.save(session);
    }

    // 根据令牌查找会话
    public LoginSession findByToken(String token) {
        Optional<LoginSession> session = loginSessionRepository.findBySessionToken(token);
        if (session.isPresent() && isSessionValid(session.get())) {
            // 更新最后活动时间
            LoginSession ls = session.get();
            ls.setLastActivityAt(LocalDateTime.now());
            return loginSessionRepository.save(ls);
        }
        return null;
    }

    // 撤销会话
    public void revokeSession(String token) {
        loginSessionRepository.revokeSession(token, LocalDateTime.now());
    }

    // 撤销用户的所有会话
    public void revokeAllUserSessions(UUID userId) {
        loginSessionRepository.revokeAllUserSessions(userId, LocalDateTime.now());
    }

    // 获取用户的活跃会话
    public List<LoginSession> getActiveSessionsByUserId(UUID userId) {
        return loginSessionRepository.findActiveSessionsByUserId(userId, LocalDateTime.now());
    }

    // 清理过期会话
    public void cleanupExpiredSessions() {
        loginSessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }

    // 验证会话是否有效
    public boolean isSessionValid(LoginSession session) {
        if (session == null) {
            return false;
        }

        // 检查是否已撤销
        if (session.getRevokedAt() != null) {
            return false;
        }

        // 检查是否过期
        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    // 延长会话时间
    public LoginSession extendSession(String token, int additionalSeconds) {
        Optional<LoginSession> session = loginSessionRepository.findBySessionToken(token);
        if (session.isPresent() && isSessionValid(session.get())) {
            LoginSession ls = session.get();
            ls.setExpiresAt(ls.getExpiresAt().plusSeconds(additionalSeconds));
            ls.setLastActivityAt(LocalDateTime.now());
            return loginSessionRepository.save(ls);
        }
        return null;
    }

    // 统计用户活跃会话数
    public long countActiveSessionsByUserId(UUID userId) {
        return loginSessionRepository.countActiveSessionsByUserId(userId, LocalDateTime.now());
    }

    // 获取当前请求
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    // 获取客户端IP地址
    private String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "Unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // 根据设备ID查找会话
    public List<LoginSession> findByDeviceId(String deviceId) {
        return loginSessionRepository.findByDeviceId(deviceId);
    }

    // 检查会话令牌是否存在
    public boolean existsByToken(String token) {
        return loginSessionRepository.existsBySessionToken(token);
    }
}