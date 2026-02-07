package com.sindikbackend.auth.dtos.requests;


import com.sindikbackend.auth.entities.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private Role role;
}

