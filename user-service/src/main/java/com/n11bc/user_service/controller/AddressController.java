package com.n11bc.user_service.controller;

import com.n11bc.user_service.dto.request.AddressRequest;
import com.n11bc.user_service.dto.response.AddressResponse;
import com.n11bc.user_service.dto.response.ErrorResponse;
import com.n11bc.user_service.dto.response.MessageResponse;
import com.n11bc.user_service.service.AddressService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Addresses", description = "Kullanici adres yonetimi")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Adres olustur",
            description = "Kullanici icin yeni adres olusturur. Ilk adres veya defaultAddress=true ise varsayilan adres olarak isaretlenir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adres basariyla olusturuldu",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @PathVariable String userId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Create address request for user: {}", userId);
        AddressResponse address = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @Operation(summary = "Kullanicinin tum adreslerini getir")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adres listesi",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Kullanici bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses(@PathVariable String userId) {
        log.info("Get all addresses request for user: {}", userId);
        List<AddressResponse> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "ID ile adres getir")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adres bilgileri",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adres bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            @PathVariable String userId,
            @PathVariable String addressId) {
        log.info("Get address {} for user: {}", addressId, userId);
        AddressResponse address = addressService.getAddressById(userId, addressId);
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Varsayilan adresi getir")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Varsayilan adres",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Varsayilan adres bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@PathVariable String userId) {
        log.info("Get default address for user: {}", userId);
        AddressResponse address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Adres guncelle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guncellenmis adres",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Gecersiz istek",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adres bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable String userId,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Update address {} for user: {}", addressId, userId);
        AddressResponse address = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Adres sil",
            description = "Silinen adres varsayilan ise kalan adreslerden biri otomatik olarak varsayilan yapilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adres basariyla silindi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adres bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<MessageResponse> deleteAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        log.info("Delete address {} for user: {}", addressId, userId);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(new MessageResponse("Address deleted successfully"));
    }

    @Operation(summary = "Varsayilan adresi degistir",
            description = "Belirtilen adresi varsayilan yapar, diger adreslerin varsayilanligini kaldirir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Varsayilan adres guncellendi",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Kimlik dogrulama gerekli",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adres bulunamadi",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{addressId}/set-default")
    public ResponseEntity<MessageResponse> setDefaultAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        log.info("Set address {} as default for user: {}", addressId, userId);
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(new MessageResponse("Address set as default successfully"));
    }
}
