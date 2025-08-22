package com.royal_medical.admin_service.controller;

import com.royal_medical.admin_service.dto.AdminDto;
import com.royal_medical.admin_service.dto.CustomerDto;
import com.royal_medical.admin_service.dto.UserDto;
import com.royal_medical.admin_service.model.Admin;
import com.royal_medical.admin_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    //    Checked
    @GetMapping("/customer/{email}")
    public ResponseEntity<?> fetchCustomerByEmail(@PathVariable String email) {
        return adminService.getCustomerByEmail(email);
    }


    //    Checked
    @GetMapping("/allcustomers")
    public ResponseEntity<List<CustomerDto>> getCustomers(){
        return adminService.getAllCustomers();
    }


    //    Checked
    //    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<AdminDto> getAllAdmins() {
        return adminService.getAllAdmins();
    }


    //    Checked
    @PostMapping("/register")
    public String registerAdmin(@RequestBody Admin admin){
        return adminService.registerAdmin(admin);
    }


    //    Checked
    @GetMapping("/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard";
    }


    //    Checked
    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getAdminByUsername(@PathVariable String username) {
        UserDto userDto = adminService.getByUsername(username);
        return ResponseEntity.ok(userDto);
    }


    //    Checked
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateAdmin(@PathVariable Long id, @RequestBody AdminDto adminDto) {
        return adminService.updateAdmin(id, adminDto);
    }


    //    Checked
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        return adminService.deleteAdmin(id);
    }


    //    Checked
    @PutMapping("/customer/update/{id}")
    public ResponseEntity<String> updateCustomerByAdmin(@PathVariable Long id, @RequestBody CustomerDto customerDto) {
        return adminService.updateCustomerByAdmin(id, customerDto);
    }


    //    Checked
    @DeleteMapping("/customer/delete/{id}")
    public ResponseEntity<String> deleteCustomerByAdmin(@PathVariable Long id) {
        return adminService.deleteCustomerByAdmin(id);
    }


    //    Checked
    @GetMapping("/search/customer")
    public ResponseEntity<List<CustomerDto>> searchCustomers(@RequestParam String keyword) {
        return adminService.searchCustomers(keyword);
    }

    //    Checked
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return adminService.getDashboardStats();
    }



}
