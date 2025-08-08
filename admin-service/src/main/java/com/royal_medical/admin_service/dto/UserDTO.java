package com.royal_medical.admin_service.dto;


import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String userType;

    public UserDTO(String username, String password, String userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }
}
