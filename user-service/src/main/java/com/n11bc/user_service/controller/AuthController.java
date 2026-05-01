package com.n11bc.user_service.controller;

import com.n11bc.user_service.dto.request.LoginRequest;
import com.n11bc.user_service.dto.request.RefreshTokenRequest;
import com.n11bc.user_service.dto.request.SignupRequest;
import com.n11bc.user_service.dto.response.ErrorResponse;
import com.n11bc.user_service.dto.response.JwtResponse;
import com.n11bc.user_service.dto.response.MessageResponse;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.service.AuthService;
import com.n11bc.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Kullanici kimlik dogrulama ve token yonetimi")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Kullanici kayit", description = "Yeni kullanici olusturur. Varsayilan rol CUSTOMER'dir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kullanici basariyla olusturuldu",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek (validation hatasi)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Kullanici adi veya email zaten mevcut",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup request received for username: {}", signupRequest.getUsername());
        UserResponse userResponse = userService.registerUser(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @Operation(summary = "Kullanici girisi", description = "Kullanici adi veya email ile giris yapar, JWT token dondurur.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Basarili giris",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Gecersiz kimlik bilgileri",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Signin request received for: {}", loginRequest.getUsernameOrEmail());
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @Operation(summary = "Token yenileme", description = "Refresh token kullanarak yeni access token alir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token basariyla yenilendi",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Gecersiz veya suresi dolmus refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Refresh token iptal edilmis",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        JwtResponse jwtResponse = authService.refreshAccessToken(request);
        return ResponseEntity.ok(jwtResponse);
    }

    @Operation(summary = "Cikis", description = "Kullanicinin refresh token'ini iptal eder. X-User-Id header'i gereklidir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Basariyla cikis yapildi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("X-User-Id") String userId) {
        log.info("Logout request received for user: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok(new MessageResponse("User logged out successfully"));
    }
}
