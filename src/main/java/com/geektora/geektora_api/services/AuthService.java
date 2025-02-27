package com.geektora.geektora_api.services;


import com.geektora.geektora_api.DTO.request.LoginRequest;
import com.geektora.geektora_api.DTO.request.RegisterRequest;

public interface AuthService {
    String login(LoginRequest loginRequest);
    String register(RegisterRequest registerRequest);
    String verify(String email, String code);
}
