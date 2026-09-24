package com.example.SpringSecurity.model;

import lombok.Data;

@Data
public class RegisterRequest {
    public String username;
    public String password;
    public Role role; // опционально
}
