package com.example.springboot002.demos.web.Service;

import com.example.springboot002.demos.web.Repository.UserRepository;
import com.example.springboot002.demos.web.Util.JwtUtil;
import com.example.springboot002.demos.web.Util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtUtil jwtUtil;

}
