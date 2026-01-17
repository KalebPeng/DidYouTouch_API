package com.example.springboot002.demos.web.Entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth_providers",
        indexes = {
                @Index(name = "idx_oauth_user", columnList = "user_id"),
                @Index(name = "idx_oauth_provider_user", columnList = "provider, provider_user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_oauth_provider_user", columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name = "uk_user_provider", columnNames = {"user_id", "provider"})
        })
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = {"accessToken", "refreshToken", "user"})
public class OAuthProvider {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_oauth_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20, nullable = false)
    private Provider provider;

    @Column(name = "provider_user_id", length = 100, nullable = false)
    private String providerUserId;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "profile_data", columnDefinition = "TEXT")
    private String profileData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Provider {
        WECHAT, GOOGLE, APPLE, GITHUB, FACEBOOK, TWITTER
    }
}