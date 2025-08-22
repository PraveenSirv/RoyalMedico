package com.royal_medical.admin_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String password;
    private String email;
    private String role;
}
