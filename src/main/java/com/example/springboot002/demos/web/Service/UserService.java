package com.example.springboot002.demos.web.Service;

import com.example.springboot002.demos.web.Entity.User;
import com.example.springboot002.demos.web.Repository.UserRepository;
import com.example.springboot002.demos.web.Util.JwtUtil;
import com.example.springboot002.demos.web.Util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @PersistenceContext
    private EntityManager entityManager;

    // 创建用户
    public User createUser(User user) {
        user.setId(UUID.randomUUID());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // 根据ID查找用户
    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    // 根据邮箱查找用户
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // 根据电话查找用户
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone).orElse(null);
    }

    // 检查邮箱是否存在
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // 检查电话是否存在
    public boolean existsByPhone(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    // 更新用户信息
    public User updateUser(UUID id, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 更新允许修改的字段
            if (updatedUser.getNickname() != null) {
                user.setNickname(updatedUser.getNickname());
            }
            if (updatedUser.getAvatarUrl() != null) {
                user.setAvatarUrl(updatedUser.getAvatarUrl());
            }
            if (updatedUser.getGender() != null) {
                user.setGender(updatedUser.getGender());
            }
            if (updatedUser.getBirthdate() != null) {
                user.setBirthdate(updatedUser.getBirthdate());
            }
            if (updatedUser.getLanguage() != null) {
                user.setLanguage(updatedUser.getLanguage());
            }
            if (updatedUser.getTimezone() != null) {
                user.setTimezone(updatedUser.getTimezone());
            }

            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }

    // 更新密码
    public void updatePassword(UUID userId, String newPasswordHash) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setPasswordHash(newPasswordHash);
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    // 更新最后登录时间
    public void updateLastLogin(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setLastLoginTime(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    // 增加登录失败次数
    public void incrementFailedLoginAttempts(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setFailedLoginAttempts(u.getFailedLoginAttempts() + 1);
            userRepository.save(u);
        }
    }

    // 重置登录失败次数
    public void resetFailedLoginAttempts(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setFailedLoginAttempts(0);
            userRepository.save(u);
        }
    }

    // 锁定账户
    public void lockAccount(UUID userId, int minutes) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
            userRepository.save(u);
        }
    }

    // 解锁账户
    public void unlockAccount(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setLockedUntil(null);
            u.setFailedLoginAttempts(0);
            userRepository.save(u);
        }
    }

    // 激活用户
    public void activateUser(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setIsActive(true);
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    // 停用用户
    public void deactivateUser(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setIsActive(false);
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    // 软删除用户
    public void softDeleteUser(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            u.setIsActive(false);
            u.setDeletedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    // 获取所有活跃用户
    public List<User> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    // 获取所有管理员
    public List<User> findAllAdmins() {
        return userRepository.findAllAdmins();
    }

    // 统计用户数量
    public long countUsers() {
        return userRepository.count();
    }

    // 统计活跃用户数量
    public long countActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }

    // 检查用户是否为管理员
    public boolean isAdmin(User user) {
        return user.getAccountType() == User.AccountType.admin;
    }

}
