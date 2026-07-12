package com.example.SpringSecurity.model;

import lombok.Data;

@Data
public class LoginRequest {
    public String username;
    public String password;
}
