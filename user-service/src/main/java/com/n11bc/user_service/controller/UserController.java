package com.n11bc.user_service.controller;

import com.n11bc.user_service.dto.request.ChangePasswordRequest;
import com.n11bc.user_service.dto.request.UpdateUserRequest;
import com.n11bc.user_service.dto.response.ErrorResponse;
import com.n11bc.user_service.dto.response.MessageResponse;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Kullanici profil yonetimi")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Tum kullanicilari listele", description = "Sadece ADMIN rolune sahip kullanicilar erisebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kullanici listesi",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Yetki yetersiz",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Get all users request");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "ID ile kullanici getir")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kullanici bilgileri",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Get user by ID request: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Kullanici guncelle", description = "Email, ad, soyad ve telefon numarasi guncellenebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guncellenmis kullanici bilgileri",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email zaten kullaniliyor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Update user request for ID: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }


    @Operation(summary = "Kullanici sifresini degistir", description = "Kullanici mevcut sifresini dogrulayarak yeni sifre belirler.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sifre basariyla guncellendi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek veya hatali mevcut sifre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<MessageResponse> changePassword(
            @PathVariable String id,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for ID: {}", id);
        userService.changePassword(id, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @Operation(summary = "Kullanici sil", description = "Sadece ADMIN rolune sahip kullanicilar erisebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kullanici basariyla silindi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Yetki yetersiz",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable String id) {
        log.info("Delete user request for ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    @Operation(summary = "Kullanici aktif et", description = "Sadece ADMIN rolune sahip kullanicilar erisebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kullanici aktif edildi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Yetki yetersiz",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable String id) {
        log.info("Activate user request for ID: {}", id);
        userService.activateUser(id);
        return ResponseEntity.ok(new MessageResponse("User activated successfully"));
    }

    @Operation(summary = "Kullanici pasif et", description = "Sadece ADMIN rolune sahip kullanicilar erisebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kullanici pasif edildi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Yetki yetersiz",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable String id) {
        log.info("Deactivate user request for ID: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.ok(new MessageResponse("User deactivated successfully"));
    }
}
