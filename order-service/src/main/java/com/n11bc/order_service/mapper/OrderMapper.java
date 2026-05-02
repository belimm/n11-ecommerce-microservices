package com.n11bc.order_service.mapper;

import com.n11bc.order_service.dto.response.CartItemSnapshotResponse;
import com.n11bc.order_service.dto.response.OrderItemResponse;
import com.n11bc.order_service.dto.response.OrderResponse;
import com.n11bc.order_service.dto.response.ShippingAddressResponse;
import com.n11bc.order_service.dto.response.UserAddressResponse;
import com.n11bc.order_service.entity.Order;
import com.n11bc.order_service.entity.OrderItem;
import com.n11bc.order_service.entity.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "shippingAddress", source = "shippingAddress")
    OrderResponse toResponse(Order order);

    ShippingAddressResponse toResponse(ShippingAddress shippingAddress);

    @Mapping(target = "productName", source = "productNameSnapshot")
    @Mapping(target = "productImageUrl", source = "productImageUrlSnapshot")
    @Mapping(target = "unitPrice", source = "unitPriceSnapshot")
    OrderItemResponse toResponse(OrderItem orderItem);

    @Mapping(target = "sourceAddressId", source = "id")
    ShippingAddress toShippingAddress(UserAddressResponse address);

    default List<OrderItem> toOrderItems(List<CartItemSnapshotResponse> cartItems) {
        return cartItems.stream()
                .map(this::toOrderItem)
                .toList();
    }

    default OrderItem toOrderItem(CartItemSnapshotResponse cartItem) {
        return OrderItem.builder()
                .productId(cartItem.productId())
                .productNameSnapshot(cartItem.productName())
                .productImageUrlSnapshot(cartItem.productImageUrl())
                .unitPriceSnapshot(cartItem.unitPrice())
                .quantity(cartItem.quantity())
                .lineTotal(cartItem.lineTotal())
                .build();
    }

    default BigDecimal calculateTotal(List<CartItemSnapshotResponse> cartItems) {
        return cartItems.stream()
                .map(CartItemSnapshotResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
