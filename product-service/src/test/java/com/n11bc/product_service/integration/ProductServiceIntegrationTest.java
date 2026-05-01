package com.n11bc.product_service.integration;

import com.n11bc.product_service.dto.request.CategoryRequest;
import com.n11bc.product_service.dto.request.LocalizedContentRequest;
import com.n11bc.product_service.dto.request.ProductCreateRequest;
import com.n11bc.product_service.dto.response.PageResponse;
import com.n11bc.product_service.dto.response.ProductResponse;
import com.n11bc.product_service.dto.response.ProductSummaryResponse;
import com.n11bc.product_service.repository.CategoryRepository;
import com.n11bc.product_service.repository.ProductRepository;
import com.n11bc.product_service.service.CategoryService;
import com.n11bc.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "app.seed.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/realms/test"
})
class ProductServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("product_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("product detail uses Accept-Language translation and category translation")
    void productDetail_localizedByAcceptLanguage() {
        categoryService.createCategory(new CategoryRequest(
                "Herbal Pantry",
                "herbal-pantry",
                "Natural pantry staples",
                List.of(new LocalizedContentRequest("tr", "Bitkisel Kiler", "Dogal temel mutfak urunleri"))
        ), "en");

        ProductResponse created = productService.createProduct(new ProductCreateRequest(
                "Cold-Pressed Olive Oil",
                "cold-pressed-olive-oil",
                "Bright extra virgin olive oil",
                new BigDecimal("249.90"),
                "https://example.com/olive-oil.jpg",
                true,
                "herbal-pantry",
                List.of(new LocalizedContentRequest("tr", "Soguk Sikim Zeytinyagi", "Canli naturel sizma zeytinyagi"))
        ), "en");

        ProductResponse localized = productService.getProductById(created.id(), "tr-TR");

        assertThat(localized.name()).isEqualTo("Soguk Sikim Zeytinyagi");
        assertThat(localized.description()).isEqualTo("Canli naturel sizma zeytinyagi");
        assertThat(localized.category().name()).isEqualTo("Bitkisel Kiler");
    }

    @Test
    @DisplayName("product listing supports category filtering and fallback locale")
    void productListing_filtersByCategoryAndFallsBackToDefaultContent() {
        categoryService.createCategory(new CategoryRequest(
                "Wellness",
                "wellness",
                "Thoughtful wellness goods",
                List.of(new LocalizedContentRequest("tr", "Iyi Yasam", "Dengeli rutinler icin urunler"))
        ), "en");

        productService.createProduct(new ProductCreateRequest(
                "Calm Herbal Tea",
                "calm-herbal-tea",
                "Caffeine-free lemon balm and chamomile blend",
                new BigDecimal("99.90"),
                "https://example.com/tea.jpg",
                true,
                "wellness",
                List.of(new LocalizedContentRequest("tr", "Sakin Bitki Cayi", "Melisa ve papatya karisimi"))
        ), "en");

        PageResponse<ProductSummaryResponse> page = productService.getProducts(0, 12, "wellness", "de-DE");

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).name()).isEqualTo("Calm Herbal Tea");
        assertThat(page.items().get(0).category().name()).isEqualTo("Wellness");
    }


    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .build();
        }
    }

}
