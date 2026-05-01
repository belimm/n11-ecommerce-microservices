package com.n11bc.cart_service.service;

import com.n11bc.cart_service.dto.request.AddCartItemRequest;
import com.n11bc.cart_service.dto.request.UpdateCartItemRequest;
import com.n11bc.cart_service.dto.response.CartResponse;
import com.n11bc.cart_service.dto.response.ProductSnapshotResponse;
import com.n11bc.cart_service.entity.Cart;
import com.n11bc.cart_service.entity.CartItem;
import com.n11bc.cart_service.entity.CartStatus;
import com.n11bc.cart_service.exception.CartItemNotFoundException;
import com.n11bc.cart_service.exception.InvalidCartOperationException;
import com.n11bc.cart_service.mapper.CartMapper;
import com.n11bc.cart_service.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProductCatalogClient productCatalogClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .id(1L)
                .userId("user-1")
                .status(CartStatus.ACTIVE)
                .lastActivityAt(LocalDateTime.now())
                .build();
        cartResponse = new CartResponse(cart.getId(), cart.getUserId(), cart.getStatus(), java.util.List.of(), BigDecimal.ZERO, cart.getLastActivityAt(), null, null);
    }

    @Test
    @DisplayName("getCurrentCart creates active cart when none exists")
    void getCurrentCart_createsCart() {
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(cartRepository.findByUserIdAndStatus("user-1", CartStatus.ACTIVE)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

        CartResponse result = cartService.getCurrentCart();

        assertThat(result).isEqualTo(cartResponse);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("addItem creates a new cart item from product snapshot")
    void addItem_newItem() {
        AddCartItemRequest request = new AddCartItemRequest(10L, 2);
        ProductSnapshotResponse product = new ProductSnapshotResponse(10L, "Olive Oil", "olive-oil", "Nice", new BigDecimal("99.90"), "image.jpg", true);
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(cartRepository.findByUserIdAndStatus("user-1", CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(productCatalogClient.getProductSnapshot(10L, "tr")).thenReturn(product);
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

        CartResponse result = cartService.addItem(request, "tr");

        assertThat(result).isEqualTo(cartResponse);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.getItems().get(0).getUnitPriceSnapshot()).isEqualByComparingTo("99.90");
    }

    @Test
    @DisplayName("addItem increments quantity for an existing item")
    void addItem_existingItemIncrementsQuantity() {
        CartItem item = CartItem.builder()
                .productId(10L)
                .productNameSnapshot("Olive Oil")
                .productImageUrlSnapshot("image.jpg")
                .unitPriceSnapshot(new BigDecimal("99.90"))
                .quantity(1)
                .build();
        cart.addItem(item);
        AddCartItemRequest request = new AddCartItemRequest(10L, 3);
        ProductSnapshotResponse product = new ProductSnapshotResponse(10L, "Olive Oil", "olive-oil", "Nice", new BigDecimal("99.90"), "image.jpg", true);
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(cartRepository.findByUserIdAndStatus("user-1", CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(productCatalogClient.getProductSnapshot(10L, "en")).thenReturn(product);
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

        cartService.addItem(request, "en");

        assertThat(item.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("updateItem updates existing item quantity")
    void updateItem_success() {
        CartItem item = CartItem.builder().productId(10L).productNameSnapshot("Tea").productImageUrlSnapshot("img").unitPriceSnapshot(BigDecimal.TEN).quantity(1).build();
        cart.addItem(item);
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(cartRepository.findByUserIdAndStatus("user-1", CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(cartResponse);

        cartService.updateItem(10L, new UpdateCartItemRequest(5));

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("updateItem rejects missing item")
    void updateItem_missingItem() {
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(cartRepository.findByUserIdAndStatus("user-1", CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItem(10L, new UpdateCartItemRequest(2)))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    @DisplayName("addItem rejects invalid quantity")
    void addItem_invalidQuantity() {
        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(10L, 0), "en"))
                .isInstanceOf(InvalidCartOperationException.class);
    }
}
