package com.n11bc.stock_service.repository;

import com.n11bc.stock_service.entity.StockReservation;
import com.n11bc.stock_service.entity.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    boolean existsByOrderIdAndStatus(Long orderId, StockReservationStatus status);
}
