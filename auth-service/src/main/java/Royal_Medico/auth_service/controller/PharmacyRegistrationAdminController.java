package Royal_Medico.auth_service.controller;

import Royal_Medico.auth_service.dto.*;
import Royal_Medico.auth_service.entity.RegistrationStatus;
import Royal_Medico.auth_service.entity.Role;
import Royal_Medico.auth_service.entity.User;
import Royal_Medico.auth_service.repository.UserRepository;
import Royal_Medico.auth_service.service.PharmacyRegistrationService;
import Royal_Medico.auth_service.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pharmacy-registrations")
@Tag(name = "Super Admin Pharmacy Registration", description = "Endpoints for Super Admin initiated pharmacy onboarding, licensing verification, and identity reservation")
public class PharmacyRegistrationAdminController {

    private static final Logger log = LoggerFactory.getLogger(PharmacyRegistrationAdminController.class);

    private final PharmacyRegistrationService registrationService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public PharmacyRegistrationAdminController(
            PharmacyRegistrationService registrationService,
            JwtUtil jwtUtil,
            UserRepository userRepository) {
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Initiate pharmacy registration and reserve statutory identities (Super Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pharmacy registration created and identities reserved"),
            @ApiResponse(responseCode = "400", description = "Validation failure or slug conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a Super Admin"),
            @ApiResponse(responseCode = "409", description = "Conflict - Statutory identifier already reserved or active")
    })
    @PostMapping
    public ResponseEntity<PharmacyRegistrationResponse> initiateRegistration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid PharmacyRegistrationRequest request) {
        User adminUser = resolveSuperAdminUser(authHeader);
        PharmacyRegistrationResponse response = registrationService.initiateRegistration(request, adminUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Verify pharmacy business & statutory licenses (Super Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pharmacy registration and identities verified"),
            @ApiResponse(responseCode = "400", description = "Invalid registration state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a Super Admin"),
            @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @PostMapping("/{id}/verify")
    public ResponseEntity<PharmacyRegistrationResponse> verifyRegistration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody @Valid VerifyRegistrationRequest request) {
        User adminUser = resolveSuperAdminUser(authHeader);
        return ResponseEntity.ok(registrationService.verifyRegistration(id, request, adminUser));
    }

    @Operation(summary = "Reject pharmacy registration and release statutory identities (Super Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pharmacy registration rejected and identities released"),
            @ApiResponse(responseCode = "400", description = "Invalid registration state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a Super Admin"),
            @ApiResponse(responseCode = "404", description = "Registration not found")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<PharmacyRegistrationResponse> rejectRegistration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody @Valid RejectRegistrationRequest request) {
        User adminUser = resolveSuperAdminUser(authHeader);
        return ResponseEntity.ok(registrationService.rejectRegistration(id, request, adminUser));
    }

    @Operation(summary = "Get pharmacy registration by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PharmacyRegistrationResponse> getRegistration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long id) {
        resolveSuperAdminUser(authHeader);
        return ResponseEntity.ok(registrationService.getRegistration(id));
    }

    @Operation(summary = "List pharmacy registrations by status")
    @GetMapping
    public ResponseEntity<List<PharmacyRegistrationResponse>> listRegistrations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", required = false) RegistrationStatus status) {
        resolveSuperAdminUser(authHeader);
        return ResponseEntity.ok(registrationService.listRegistrations(status));
    }

    private User resolveSuperAdminUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired JWT token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found: " + email));

        if (user.getRole() != Role.SUPER_ADMIN) {
            log.warn("UNAUTHORIZED_ADMIN_ACCESS: User id={} with role={} attempted to access Super Admin endpoint", 
                    user.getId(), user.getRole());
            throw new AccessDeniedException("Forbidden: Only Super Admin can access pharmacy registration management");
        }

        return user;
    }
}

