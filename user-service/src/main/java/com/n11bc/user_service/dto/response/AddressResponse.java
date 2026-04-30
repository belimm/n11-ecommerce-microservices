package com.n11bc.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String id;

    private String title;

    private String street;

    private String city;

    private String country;

    private String zipCode;

    private boolean defaultAddress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
