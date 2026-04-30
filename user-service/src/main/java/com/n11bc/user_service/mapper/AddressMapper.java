package com.n11bc.user_service.mapper;

import com.n11bc.user_service.dto.request.AddressRequest;
import com.n11bc.user_service.dto.response.AddressResponse;
import com.n11bc.user_service.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    // AddressRequest to Address entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address addressRequestToAddress(AddressRequest request);

    // Address entity to AddressResponse
    AddressResponse addressToAddressResponse(Address address);

    // UpdateAddressRequest to Address (partial update)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAddressFromRequest(AddressRequest request, @MappingTarget Address address);
}
