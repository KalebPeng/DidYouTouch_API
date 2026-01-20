package com.example.springboot002.demos.web.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String nickname;
    private String token;
    private String message;
}