package com.n11bc.stock_service.mapper;

import com.n11bc.stock_service.dto.response.InventoryResponse;
import com.n11bc.stock_service.entity.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    InventoryResponse toResponse(Inventory inventory);
}
