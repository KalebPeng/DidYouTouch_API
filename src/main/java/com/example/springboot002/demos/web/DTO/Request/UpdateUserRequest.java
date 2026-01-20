package com.example.springboot002.demos.web.DTO.Request;

import com.example.springboot002.demos.web.Entity.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public class UpdateUserRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    
    private String email;

    private String nickname;
    
    private String phone;
    
    private String gender;
    
    private LocalDate birthdate;
    
    private String avatarUrl;

    public UpdateUserRequest(UUID id, String username, String nickname, String avatarUrl, User.AccountType accountType) {
    }
}
