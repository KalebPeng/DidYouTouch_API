package com.example.springboot002.demos.web.Entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_sessions",
        indexes = {
                @Index(name = "idx_sessions_user", columnList = "user_id"),
                @Index(name = "idx_sessions_token", columnList = "session_token"),
                @Index(name = "idx_sessions_device", columnList = "device_id"),
                @Index(name = "idx_sessions_expiry", columnList = "expires_at"),
                @Index(name = "idx_sessions_active", columnList = "is_active, expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sessions_token", columnNames = "session_token")
        })
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class LoginSession {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sessions_user"))
    private User user;

    @Column(name = "session_token", length = 500, nullable = false, unique = true)
    private String sessionToken;  // JWT token 可能很长，建议500

    @Column(name = "device_id", length = 100, nullable = false)
    private String deviceId;

    @Column(name = "device_type", length = 20)
    private String deviceType;  // Android, iOS, Web

    @Column(name = "device_name", length = 100)
    private String deviceName;  // Xiaomi 12, iPhone 14 Pro

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "os_version", length = 50)
    private String osVersion;  // Android 13, iOS 17.2

    @Column(name = "app_version", length = 20)
    private String appVersion;  // 1.0.0

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;  // IPv6 最长45字符

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // 改为 TEXT 类型，存储 JSON 字符串
    @Column(name = "location", columnDefinition = "TEXT")
    private String location;  // 存储 JSON: {"country":"中国","city":"北京"}

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivityAt = LocalDateTime.now();
    }

    // 业务方法
    public boolean isValid() {
        return isActive &&
                revokedAt == null &&
                expiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void extendExpiration(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }
}