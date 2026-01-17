package com.example.springboot002.demos.web.Service;

import com.example.springboot002.demos.web.Entity.UserInfo;
import com.example.springboot002.demos.web.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // 获取所有用户
    public List<UserInfo> getAllUsers() {
        return userInfoRepository.findAll();
    }

    // 根据ID获取用户
    public Optional<UserInfo> getUserById(Integer id) {
        return userInfoRepository.findById(id);
    }

    // 创建用户
    public UserInfo createUser(UserInfo userInfo) {
        // 验证用户名唯一性
        if (userInfoRepository.existsByUsername(userInfo.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 验证邮箱唯一性
        if (userInfoRepository.existsByEmail(userInfo.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 设置默认值
        if (userInfo.getIsActive() == null) {
            userInfo.setIsActive(true);
        }

        if (userInfo.getIsAdmin() == null) {
            userInfo.setIsAdmin(false);
        }

        // 密码加密（如果有密码）
        if (userInfo.getPasswordHash() != null && passwordEncoder != null) {
            userInfo.setPasswordHash(passwordEncoder.encode(userInfo.getPasswordHash()));
        }

        return userInfoRepository.save(userInfo);
    }

    // 更新用户
    public UserInfo updateUser(Integer id, UserInfo userDetails) {
        return userInfoRepository.findById(id)
                .map(existingUser -> {
                    // 更新基本信息
                    if (userDetails.getName() != null) {
                        existingUser.setName(userDetails.getName());
                    }

                    if (userDetails.getEmail() != null &&
                            !existingUser.getEmail().equals(userDetails.getEmail())) {
                        // 检查新邮箱是否已被其他用户使用
                        if (userInfoRepository.existsByEmail(userDetails.getEmail())) {
                            throw new RuntimeException("邮箱已被其他用户使用");
                        }
                        existingUser.setEmail(userDetails.getEmail());
                    }

                    if (userDetails.getPhone() != null) {
                        existingUser.setPhone(userDetails.getPhone());
                    }

                    if (userDetails.getAvatarUrl() != null) {
                        existingUser.setAvatarUrl(userDetails.getAvatarUrl());
                    }

                    if (userDetails.getIsActive() != null) {
                        existingUser.setIsActive(userDetails.getIsActive());
                    }

                    if (userDetails.getIsAdmin() != null) {
                        existingUser.setIsAdmin(userDetails.getIsAdmin());
                    }

                    // 更新密码（如果有）
                    if (userDetails.getPasswordHash() != null && passwordEncoder != null) {
                        existingUser.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
                    }

                    return userInfoRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 删除用户（软删除）
    public void deleteUser(Integer id) {
        userInfoRepository.findById(id)
                .ifPresent(user -> {
                    user.setIsActive(false);
                    user.setDeletedAt(LocalDateTime.now());
                    userInfoRepository.save(user);
                });
    }

    // 彻底删除用户
    public void permanentDeleteUser(Integer id) {
        userInfoRepository.deleteById(id);
    }

    // 根据用户名查找用户
    public Optional<UserInfo> getUserByUsername(String username) {
        return userInfoRepository.findByUsername(username);
    }

    // 根据邮箱查找用户
    public Optional<UserInfo> getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email);
    }

    // 获取活跃用户
    public List<UserInfo> getActiveUsers() {
        return userInfoRepository.findByIsActive(true);
    }

    // 搜索用户
    public List<UserInfo> searchUsers(String keyword) {
        return userInfoRepository.findByNameContaining(keyword);
    }

    // 统计活跃用户数量
    public Long countActiveUsers() {
        return userInfoRepository.countActiveUsers();
    }

    // 更新最后登录时间
    public void updateLastLogin(Integer userId) {
        userInfoRepository.findById(userId)
                .ifPresent(user -> {
                    user.setLastLoginAt(LocalDateTime.now());
                    userInfoRepository.save(user);
                });
    }
}