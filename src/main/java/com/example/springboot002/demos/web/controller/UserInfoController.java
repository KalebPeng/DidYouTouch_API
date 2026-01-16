package com.example.springboot002.demos.web.controller;

import com.example.springboot002.demos.web.Entity.UserInfo;
import com.example.springboot002.demos.web.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的CRUD操作")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @Operation(summary = "获取所有用户", description = "返回所有用户的列表")
    @GetMapping
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        List<UserInfo> users = userInfoService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "找到用户"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserInfo> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Integer id) {
        return userInfoService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "创建用户", description = "创建一个新的用户")
    @PostMapping
    public ResponseEntity<?> createUser(
            @Parameter(description = "用户信息", required = true)
            @Valid @RequestBody UserInfo userInfo) {
        try {
            UserInfo createdUser = userInfoService.createUser(userInfo);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "更新用户", description = "根据ID更新用户信息")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Integer id,
            @Parameter(description = "更新的用户信息", required = true)
            @Valid @RequestBody UserInfo userDetails) {
        try {
            UserInfo updatedUser = userInfoService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "删除用户", description = "软删除用户（将用户状态设为不活跃）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Integer id) {
        userInfoService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "彻底删除用户", description = "从数据库中彻底删除用户")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDeleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Integer id) {
        userInfoService.permanentDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "根据用户名查找用户", description = "根据用户名查找用户")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserInfo> getUserByUsername(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username) {
        return userInfoService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "根据邮箱查找用户", description = "根据邮箱查找用户")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserInfo> getUserByEmail(
            @Parameter(description = "邮箱", required = true)
            @PathVariable String email) {
        return userInfoService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "搜索用户", description = "根据姓名关键词搜索用户")
    @GetMapping("/search")
    public ResponseEntity<List<UserInfo>> searchUsers(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword) {
        List<UserInfo> users = userInfoService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "获取活跃用户", description = "获取所有活跃用户")
    @GetMapping("/active")
    public ResponseEntity<List<UserInfo>> getActiveUsers() {
        List<UserInfo> users = userInfoService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "统计活跃用户数量", description = "统计系统中活跃用户的数量")
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActiveUsers() {
        Long count = userInfoService.countActiveUsers();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "更新最后登录时间", description = "更新用户的最后登录时间")
    @PostMapping("/{id}/update-last-login")
    public ResponseEntity<Void> updateLastLogin(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Integer id) {
        userInfoService.updateLastLogin(id);
        return ResponseEntity.ok().build();
    }
    //test
}