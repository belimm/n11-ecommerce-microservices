package com.n11bc.order_service.repository;

import com.n11bc.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findById(Long id);

    boolean existsByOrderNumber(String orderNumber);
}
