package com.example.springboot002.demos.web.Entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.time.LocalDateTime;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_sessions",
        indexes = {
                @Index(name = "idx_sessions_user", columnList = "user_id"),
                @Index(name = "idx_sessions_token", columnList = "session_token"),
                @Index(name = "idx_sessions_device", columnList = "device_id"),
                @Index(name = "idx_sessions_expiry", columnList = "expires_at")
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

    @Column(name = "session_token", length = 100, nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "device_id", length = 100, nullable = false)
    private String deviceId;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Type(type = "jsonb")  // PostgreSQL 使用 jsonb
    @Column(name = "location", columnDefinition = "jsonb")
    private String location;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive = true;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "last_activity_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
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

    public boolean isValid() {
        return isActive && expiresAt.isAfter(LocalDateTime.now());
    }

    public void revoke() {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
    }
}
