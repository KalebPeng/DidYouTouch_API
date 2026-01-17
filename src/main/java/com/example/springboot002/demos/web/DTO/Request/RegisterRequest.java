package com.example.springboot002.demos.web.DTO.Request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过255个字符")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度必须在8-50个字符之间")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$",
            message = "密码必须包含数字、字母和特殊字符")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Pattern(regexp = "^(male|female|unknown)$", message = "性别必须是male、female或unknown")
    private String gender = "unknown";

    private LocalDate birthdate;
}
