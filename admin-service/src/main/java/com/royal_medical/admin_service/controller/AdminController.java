package com.royal_medical.admin_service.controller;

import com.royal_medical.admin_service.client.CustomerClient;
import com.royal_medical.admin_service.dto.AdminDto;
import com.royal_medical.admin_service.dto.CustomerDto;
import com.royal_medical.admin_service.dto.UserDTO;
import com.royal_medical.admin_service.model.Admin;
import com.royal_medical.admin_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/customer/{email}")
    public ResponseEntity<?> fetchCustomerByEmail(@PathVariable String email) {
        return adminService.getCustomerByEmail(email);
    }

    @GetMapping("/allcustomers")
    public ResponseEntity<List<CustomerDto>> getCustomers(){
        return adminService.getAllCustomers();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<AdminDto> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @PostMapping("/register")
    public String registerAdmin(@RequestBody Admin admin){
        return adminService.registerAdmin(admin);
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard";
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getAdminByUsername(@PathVariable String username) {
        UserDTO userDTO = adminService.getByUsername(username);
        return ResponseEntity.ok(userDTO);
    }

}
