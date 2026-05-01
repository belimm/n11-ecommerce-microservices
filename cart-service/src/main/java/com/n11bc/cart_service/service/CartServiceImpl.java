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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CurrentUserService currentUserService;
    private final ProductCatalogClient productCatalogClient;

    @Override
    @Transactional
    public CartResponse getCurrentCart() {
        Cart cart = getOrCreateActiveCart(currentUserService.getCurrentUserId());
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(AddCartItemRequest request, String acceptLanguage) {
        validateQuantity(request.quantity());
        Cart cart = getOrCreateActiveCart(currentUserService.getCurrentUserId());
        ProductSnapshotResponse product = productCatalogClient.getProductSnapshot(request.productId(), acceptLanguage);

        cart.findItemByProductId(request.productId())
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.quantity()),
                        () -> cart.addItem(CartItem.builder()
                                .productId(product.id())
                                .productNameSnapshot(product.name())
                                .productImageUrlSnapshot(product.imageUrl())
                                .unitPriceSnapshot(product.price())
                                .quantity(request.quantity())
                                .build())
                );
        touch(cart);
        Cart savedCart = cartRepository.save(cart);
        log.info("Product {} added to cart {} for user {}", request.productId(), savedCart.getId(), savedCart.getUserId());
        return cartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long productId, UpdateCartItemRequest request) {
        validateQuantity(request.quantity());
        Cart cart = getOrCreateActiveCart(currentUserService.getCurrentUserId());
        CartItem item = cart.findItemByProductId(productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
        item.setQuantity(request.quantity());
        touch(cart);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long productId) {
        Cart cart = getOrCreateActiveCart(currentUserService.getCurrentUserId());
        CartItem item = cart.findItemByProductId(productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
        cart.removeItem(item);
        touch(cart);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse clearCart() {
        Cart cart = getOrCreateActiveCart(currentUserService.getCurrentUserId());
        cart.getItems().clear();
        touch(cart);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    private Cart getOrCreateActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .userId(userId)
                        .status(CartStatus.ACTIVE)
                        .lastActivityAt(LocalDateTime.now())
                        .build()));
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new InvalidCartOperationException("Quantity must be at least 1");
        }
    }

    private void touch(Cart cart) {
        cart.setLastActivityAt(LocalDateTime.now());
        if (cart.getStatus() == CartStatus.ABANDONED) {
            cart.setStatus(CartStatus.ACTIVE);
        }
    }
}
