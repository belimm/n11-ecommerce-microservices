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
import java.util.stream.IntStream;

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
        return IntStream.rangeClosed(1, 120)
                .mapToObj(productId -> new InventorySeed((long) productId, realisticQuantity(productId)))
                .toList();
    }

    private int realisticQuantity(int productId) {
        return 25 + ((productId * 17) % 176);
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
