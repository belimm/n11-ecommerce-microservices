package com.n11bc.user_service.dto.response;

import com.n11bc.user_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String username;

    private String email;

    private Role role;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private List<AddressResponse> addresses;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
