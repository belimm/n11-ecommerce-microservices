package com.n11bc.user_service.mapper;

import com.n11bc.user_service.dto.request.SignupRequest;
import com.n11bc.user_service.dto.request.UpdateUserRequest;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {AddressMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // SignupRequest to User entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    User signupRequestToUser(SignupRequest request);

    // User entity to UserResponse
    @Mapping(target = "addresses", ignore = true)
    UserResponse userToUserResponse(User user);

    // UpdateUserRequest to User entity (for partial updates)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
