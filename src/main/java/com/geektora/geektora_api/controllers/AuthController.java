package com.geektora.geektora_api.controllers;


import com.geektora.geektora_api.DTO.request.LoginRequest;
import com.geektora.geektora_api.DTO.request.RegisterRequest;
import com.geektora.geektora_api.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
    @PostMapping("/verify")
    public String verify(@RequestParam String email, @RequestParam String code) {
        return authService.verify(email, code);
    }

}

