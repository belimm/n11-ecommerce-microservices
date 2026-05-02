package com.n11bc.stock_service.config;

import com.n11bc.stock_service.entity.Inventory;
import com.n11bc.stock_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedInventories().forEach(this::seedInventory);
    }

    private List<InventorySeed> seedInventories() {
        return List.of(
                new InventorySeed(1L, 120),
                new InventorySeed(2L, 95),
                new InventorySeed(3L, 80),
                new InventorySeed(4L, 70),
                new InventorySeed(5L, 140),
                new InventorySeed(6L, 110),
                new InventorySeed(7L, 45),
                new InventorySeed(8L, 55),
                new InventorySeed(9L, 38),
                new InventorySeed(10L, 130),
                new InventorySeed(11L, 75),
                new InventorySeed(12L, 90)
        );
    }

    private void seedInventory(InventorySeed seed) {
        inventoryRepository.findByProductId(seed.productId())
                .ifPresentOrElse(
                        inventory -> log.debug("Inventory seed skipped for product {}", inventory.getProductId()),
                        () -> {
                            Inventory inventory = Inventory.builder()
                                    .productId(seed.productId())
                                    .availableQuantity(seed.availableQuantity())
                                    .reservedQuantity(0)
                                    .build();
                            Inventory saved = inventoryRepository.save(inventory);
                            log.info("Seeded inventory for product {} with quantity {}", saved.getProductId(), saved.getAvailableQuantity());
                        }
                );
    }

    private record InventorySeed(Long productId, int availableQuantity) {
    }
}
