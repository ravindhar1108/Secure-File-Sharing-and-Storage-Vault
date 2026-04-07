package com.example.Vault.controller;

import com.example.Vault.DTO.LoginRequest;
import com.example.Vault.DTO.SignupRequest;
import com.example.Vault.model.User;
import com.example.Vault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Vault.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService userService;
    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest)
    {
        return userService.createUser(signupRequest.getEmail(),signupRequest.getPassword());
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest)
    {
        User user = userService.login(loginRequest.getEmail(),loginRequest.getPassword());
        return jwtUtil.generateToken(user.getEmail());
    }

}
