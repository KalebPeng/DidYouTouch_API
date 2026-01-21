package com.example.springboot002.demos.web.Controller;

import com.example.springboot002.demos.web.DTO.Response.Response;
import com.example.springboot002.demos.web.Entity.User;
import com.example.springboot002.demos.web.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/users") // 添加基础路径映射
public class UserController {

    @Autowired
    private UserService userService;

    //根据邮箱查询用户信息
    @Operation(summary = "根据邮箱查找用户", description = "根据邮箱查找用户")
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@Parameter(description = "邮箱", required = true) @PathVariable String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("NOT_FOUND", "邮箱不存在"));
        }
        return ResponseEntity.ok(user);
    }

    //获取用户信息（通过用户ID）
    @Operation(summary = "根据UserID查找用户", description = "根据UserID查找用户")
    @GetMapping("/ID/{userID}")
    public ResponseEntity<?> getUserByID(@Parameter(description = "ID", required = true) @PathVariable UUID userID) {
        User user = userService.findById(userID);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("NOT_FOUND", "ID不存在"));
        }

        User userInfo = new User();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setAccountType(user.getAccountType());

        return ResponseEntity.ok(userInfo);

    }

    //更新用户基础信息
    @Operation(summary = "更新用户基础信息", description = "更新用户基础信息")
    @PutMapping("/ID/{ID}")
    public ResponseEntity<?> updateUser(@Parameter(description = "ID", required = true) @PathVariable UUID ID, @Valid @RequestBody User request) {
        try {
            // 更新用户信息
            userService.updateUser(ID, request);

            return ResponseEntity.ok(new Response("SUCCESS", "更新成功"));
        } catch (Exception e) {
            if (e.getMessage().contains("ID不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("NOT_FOUND", "ID不存在"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("UPDATE_ERROR", "更新失败: " + e.getMessage()));
        }
    }
}
