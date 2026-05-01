package com.n11bc.cart_service.mapper;

import com.n11bc.cart_service.dto.response.CartItemResponse;
import com.n11bc.cart_service.dto.response.CartResponse;
import com.n11bc.cart_service.entity.Cart;
import com.n11bc.cart_service.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "productName", source = "productNameSnapshot")
    @Mapping(target = "productImageUrl", source = "productImageUrlSnapshot")
    @Mapping(target = "unitPrice", source = "unitPriceSnapshot")
    @Mapping(target = "lineTotal", expression = "java(item.lineTotal())")
    CartItemResponse toItemResponse(CartItem item);

    @Mapping(target = "totalPrice", expression = "java(totalPrice(cart))")
    CartResponse toResponse(Cart cart);

    default BigDecimal totalPrice(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
