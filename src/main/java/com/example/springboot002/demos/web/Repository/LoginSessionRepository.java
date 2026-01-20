package com.example.springboot002.demos.web.Repository;

import com.example.springboot002.demos.web.Entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {

    // 根据会话令牌查找会话
    Optional<LoginSession> findBySessionToken(String sessionToken);

    // 查找用户的所有活跃会话
    @Query("SELECT ls FROM LoginSession ls WHERE ls.user.id = :userId AND ls.revokedAt IS NULL AND ls.expiresAt > :now")
    List<LoginSession> findActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // 查找用户的所有会话
    List<LoginSession> findByUserId(UUID userId);

    // 撤销用户的所有会话
    @Modifying
    @Query("UPDATE LoginSession ls SET ls.revokedAt = :now WHERE ls.user.id = :userId AND ls.revokedAt IS NULL")
    void revokeAllUserSessions(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // 撤销特定会话
    @Modifying
    @Query("UPDATE LoginSession ls SET ls.revokedAt = :now WHERE ls.sessionToken = :token AND ls.revokedAt IS NULL")
    void revokeSession(@Param("token") String token, @Param("now") LocalDateTime now);

    // 删除过期的会话
    @Modifying
    @Query("DELETE FROM LoginSession ls WHERE ls.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    // 统计用户的活跃会话数
    @Query("SELECT COUNT(ls) FROM LoginSession ls WHERE ls.user.id = :userId AND ls.revokedAt IS NULL AND ls.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // 根据设备ID查找会话
    List<LoginSession> findByDeviceId(String deviceId);

    // 检查会话令牌是否存在
    boolean existsBySessionToken(String sessionToken);
}