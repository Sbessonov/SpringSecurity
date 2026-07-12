package com.example.SpringSecurity.controller;

import com.example.SpringSecurity.configuration.jwt.JWTUtils;
import com.example.SpringSecurity.entity.UserEntity;
import com.example.SpringSecurity.model.Role;
import com.example.SpringSecurity.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // дабл чек
            if (!userDetails.isAccountNonLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked");
            }

            userService.resetFailedAttempts(request.getUsername());

            String accessToken = jwtUtils.generateAccessToken(userDetails);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            tokens.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

            return ResponseEntity.ok(tokens);

        } catch (BadCredentialsException e) {
            // Увеличиваем счетчик неудачных попыток
            userService.incrementFailedAttempts(request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtUtils.extractUsername(refreshToken);
        UserDetails userDetails = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (jwtUtils.isTokenValid(refreshToken, userDetails)) {
            String newAccessToken = jwtUtils.generateAccessToken(userDetails);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .name(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isLocked(false)
                .failedAttempts(0)
                .build();

        userService.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/unlock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> unlockAccount(@RequestParam String username) {
        userService.unlockAccount(username);
        return ResponseEntity.ok("Account unlocked");
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("User: " + userDetails.getUsername() +
                ", Role: " + userDetails.getAuthorities());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> adminOnly() {
        return ResponseEntity.ok("Admin panel");
    }

    @GetMapping("/moderate")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> moderatorOnly() {
        return ResponseEntity.ok("Moderator smth");
    }

    // DTO классы
    @Data
    static class LoginRequest {
        public String username;
        public String password;
    }

    @Data
    static class RefreshRequest {
        public String refreshToken;
    }

    @Data
    static class RegisterRequest {
        public String username;
        public String password;
        public Role role; // опционально
    }
}