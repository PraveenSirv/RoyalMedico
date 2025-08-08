package com.royal_medical.customer_service.dto;


import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String role;

    public UserDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
