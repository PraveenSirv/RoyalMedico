package Royal_Medico.auth_service.controller;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token refresh")
public class AuthController {

    @Autowired
    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login"),
            @ApiResponse(responseCode = "401", description = "Bad credentials or locked account")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "Logout user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }

    @Operation(summary = "Get user pharmacy memberships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of memberships retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token")
    })
    @GetMapping("/memberships")
    public ResponseEntity<List<TenantMembershipDto>> getMemberships(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.getUserMemberships(authHeader));
    }

    @Operation(summary = "Select an active tenant context and receive tenant-scoped access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenant selected and token issued"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token"),
            @ApiResponse(responseCode = "403", description = "Inactive or unauthorized tenant membership")
    })
    @PostMapping("/select-tenant")
    public ResponseEntity<TenantAuthResponse> selectTenant(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid TenantSelectRequest request) {
        return ResponseEntity.ok(authService.selectTenant(authHeader, request.getTenantId()));
    }

    @Operation(summary = "Switch tenant context and receive new tenant-scoped access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tenant switched and token issued"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token"),
            @ApiResponse(responseCode = "403", description = "Inactive or unauthorized tenant membership")
    })
    @PostMapping("/switch-tenant")
    public ResponseEntity<TenantAuthResponse> switchTenant(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid TenantSelectRequest request) {
        return ResponseEntity.ok(authService.selectTenant(authHeader, request.getTenantId()));
    }
}
