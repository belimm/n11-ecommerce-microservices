package com.n11bc.cart_service.repository;

import com.n11bc.cart_service.entity.Cart;
import com.n11bc.cart_service.entity.CartStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByUserIdAndStatus(String userId, CartStatus status);

    @EntityGraph(attributePaths = "items")
    List<Cart> findByStatusAndLastActivityAtBefore(CartStatus status, LocalDateTime threshold);
}
