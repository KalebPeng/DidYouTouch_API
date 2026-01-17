package com.example.springboot002.demos.web.DTO.Request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class PasswordResetRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

}
