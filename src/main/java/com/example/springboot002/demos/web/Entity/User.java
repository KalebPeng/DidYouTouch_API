package com.example.springboot002.demos.web.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone"),
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_status", columnList = "is_active, is_deleted"),
                @Index(name = "idx_users_created", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        })
@Data
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    //用户名
    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "salt", length = 50)
    private String salt = UUID.randomUUID().toString();

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'unknown'")
    private Gender gender = Gender.unknown;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isVerified = false;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'standard'")
    private AccountType accountType = AccountType.standard;

    @Column(name = "failed_login_attempts", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 40)
    private String lastLoginIp;

    @Column(name = "mfa_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 50)
    private String mfaSecret;

    @Column(name = "language", length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'zh_CN'")
    private String language = "zh_CN";

    @Column(name = "timezone", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'Asia/Shanghai'")
    private String timezone = "Asia/Shanghai";

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    private String notificationPreferences = "{\"email\": true, \"push\": true, \"daily_report\": true}";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;

    // 关联关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LoginSession> loginSessions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OAuthProvider> oauthProviders = new HashSet<>();

    //@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    //private Set<UserDevice> devices = new HashSet<>();

    //@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    //private UserPreferences preferences;

    public enum Gender {
        male, female, unknown
    }

    public enum AccountType {
        standard,
        premium,
        admin
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (termsAcceptedAt == null) {
            termsAcceptedAt = LocalDateTime.now();
        }
        if (privacyAcceptedAt == null) {
            privacyAcceptedAt = LocalDateTime.now();
        }
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isAccountActive() {
        return isActive && !isDeleted && !isLocked();
    }
}
