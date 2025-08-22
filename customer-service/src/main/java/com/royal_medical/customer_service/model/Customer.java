package com.royal_medical.customer_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, length = 50)
    private String username;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    private String role = "CUSTOMER";

}
