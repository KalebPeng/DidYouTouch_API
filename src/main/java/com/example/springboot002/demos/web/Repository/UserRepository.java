package com.example.springboot002.demos.web.Repository;

import com.example.springboot002.demos.web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User,UUID>
{
    //根据邮箱查找用户
    Optional<User> findByEmail(String email);

    //根据用户名查找用户
    Optional<User> findByUsername(String username);

    //根据手机查找用户
    Optional<User> findByPhone(String phone);

    //根据邮箱判断存在
    boolean existsByEmail(String email);

    //根据用户名判断存在
    boolean existsByUsername(String username);

    //根据手机判断存在
    boolean existsByPhone(String phone);

    boolean existsByEmailAndIsDeletedFalse(String email);

    //根据邮箱查找活跃用户
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true AND u.isDeleted = false")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.lockedUntil < :now AND u.isActive = true")
    List<User> findLockedUsersBefore(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end")
    Long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u FROM User u WHERE u.accountType = 'ADMIN'")
    List<User> findAllAdmins();

    // 查找所有活跃用户
    List<User> findByIsActiveTrue();

    // 统计活跃用户数量
    long countByIsActiveTrue();
}
