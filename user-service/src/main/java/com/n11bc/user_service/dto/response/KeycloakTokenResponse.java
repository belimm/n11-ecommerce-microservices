package com.n11bc.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakTokenResponse {

    private String accessToken;

    private String refreshToken;

    private int expiresIn;

    private int refreshExpiresIn;

    private String tokenType;
}
