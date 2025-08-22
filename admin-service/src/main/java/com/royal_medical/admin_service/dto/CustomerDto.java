package com.royal_medical.admin_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

    private String username;
    private String email;
    private String address;
}
