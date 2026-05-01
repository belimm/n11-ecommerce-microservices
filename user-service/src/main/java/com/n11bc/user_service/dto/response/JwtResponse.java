package com.n11bc.user_service.dto.response;

import com.n11bc.user_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    // User information
    private String id;

    private String username;

    private String email;

    private Role role;

    private String firstName;

    private String lastName;
}
