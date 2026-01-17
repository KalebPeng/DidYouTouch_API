package com.example.springboot002.demos.web.DTO.Request;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class ProfileUpdateRequest {
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    @Pattern(regexp = "^(male|female|unknown)$", message = "性别必须是male、female或unknown")
    private String gender;

    private LocalDate birthdate;

    @Size(max = 10, message = "语言长度不能超过10个字符")
    private String language;

    @Size(max = 50, message = "时区长度不能超过50个字符")
    private String timezone;
}
