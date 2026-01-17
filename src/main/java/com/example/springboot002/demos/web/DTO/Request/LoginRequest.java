package com.example.springboot002.demos.web.DTO.Request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String deviceId;

    private String deviceType = "android";

    private String deviceName;

    private String deviceModel;
}
