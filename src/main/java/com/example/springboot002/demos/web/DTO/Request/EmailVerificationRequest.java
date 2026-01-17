package com.example.springboot002.demos.web.DTO.Request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmailVerificationRequest {
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位")
    private String code;
}
