package com.example.springboot002.demos.web.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private UUID userId;
    private String email;
    private String nickname;
    private String avatar;
    private Boolean isAdmin;
    private String token;
    private LocalDateTime tokenExpiresAt;
    private String message;
}