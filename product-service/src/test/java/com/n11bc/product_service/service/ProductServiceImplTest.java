package com.n11bc.product_service.service;

import com.n11bc.product_service.dto.request.LocalizedContentRequest;
import com.n11bc.product_service.dto.request.ProductCreateRequest;
import com.n11bc.product_service.dto.request.ProductUpdateRequest;
import com.n11bc.product_service.dto.response.CategoryResponse;
import com.n11bc.product_service.dto.response.PageResponse;
import com.n11bc.product_service.dto.response.ProductResponse;
import com.n11bc.product_service.dto.response.ProductSummaryResponse;
import com.n11bc.product_service.entity.Category;
import com.n11bc.product_service.entity.Product;
import com.n11bc.product_service.exception.CategoryNotFoundException;
import com.n11bc.product_service.exception.DuplicateSlugException;
import com.n11bc.product_service.exception.ProductNotFoundException;
import com.n11bc.product_service.mapper.ProductMapper;
import com.n11bc.product_service.repository.CategoryRepository;
import com.n11bc.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;
    private ProductCreateRequest createRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Herbal Pantry")
                .slug("herbal-pantry")
                .description("Natural pantry staples")
                .build();
        product = Product.builder()
                .id(10L)
                .name("Cold-Pressed Olive Oil")
                .slug("cold-pressed-olive-oil")
                .description("Bright extra virgin olive oil")
                .price(new BigDecimal("249.90"))
                .imageUrl("https://example.com/olive-oil.jpg")
                .active(true)
                .category(category)
                .build();
        createRequest = new ProductCreateRequest(
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                true,
                category.getSlug(),
                List.of(new LocalizedContentRequest("tr", "Soguk Sikim Zeytinyagi", "Canli naturel sizma zeytinyagi"))
        );
        productResponse = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                true,
                "en",
                new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), "en", null, null),
                null,
                null
        );
    }

    @Test
    @DisplayName("createProduct creates product with category and translations")
    void createProduct_success() {
        when(productRepository.existsBySlug(createRequest.slug())).thenReturn(false);
        when(categoryRepository.findBySlug(createRequest.categorySlug())).thenReturn(Optional.of(category));
        when(productMapper.toEntity(createRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(eq(product), any())).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(createRequest, "tr-TR");

        assertThat(result).isEqualTo(productResponse);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getTranslations()).hasSize(1);
        assertThat(product.getTranslations().get(0).getLocale()).isEqualTo("tr");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("createProduct rejects duplicate slug")
    void createProduct_duplicateSlug() {
        when(productRepository.existsBySlug(createRequest.slug())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(createRequest, "en"))
                .isInstanceOf(DuplicateSlugException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct rejects unknown category")
    void createProduct_unknownCategory() {
        when(productRepository.existsBySlug(createRequest.slug())).thenReturn(false);
        when(categoryRepository.findBySlug(createRequest.categorySlug())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(createRequest, "en"))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProductById rejects unknown product")
    void getProductById_notFound() {
        when(productRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(404L, "en"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("getProducts returns paginated summaries")
    void getProducts_success() {
        ProductSummaryResponse summary = new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getImageUrl(),
                true,
                "en",
                new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), "en", null, null)
        );
        when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toSummaryResponse(eq(product), any())).thenReturn(summary);

        PageResponse<ProductSummaryResponse> result = productService.getProducts(0, 12, null, "en-US");

        assertThat(result.items()).containsExactly(summary);
        assertThat(result.totalElements()).isEqualTo(1);
    }


    @Test
    @DisplayName("getProductById returns product detail")
    void getProductById_success() {
        when(productRepository.findByIdWithDetails(product.getId())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(eq(product), any())).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(product.getId(), "en");

        assertThat(result).isEqualTo(productResponse);
    }

    @Test
    @DisplayName("getProductBySlug returns product detail")
    void getProductBySlug_success() {
        when(productRepository.findBySlug(product.getSlug())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(eq(product), any())).thenReturn(productResponse);

        ProductResponse result = productService.getProductBySlug(product.getSlug(), "en");

        assertThat(result).isEqualTo(productResponse);
    }

    @Test
    @DisplayName("getProductBySlug rejects unknown slug")
    void getProductBySlug_notFound() {
        when(productRepository.findBySlug("missing-product")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductBySlug("missing-product", "en"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("getProducts filters by category slug")
    void getProducts_withCategoryFilter() {
        ProductSummaryResponse summary = new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getImageUrl(),
                true,
                "en",
                new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), "en", null, null)
        );
        when(productRepository.findByCategorySlugAndActiveTrue(eq(category.getSlug()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toSummaryResponse(eq(product), any())).thenReturn(summary);

        PageResponse<ProductSummaryResponse> result = productService.getProducts(0, 12, category.getSlug(), "en");

        assertThat(result.items()).containsExactly(summary);
        verify(productRepository).findByCategorySlugAndActiveTrue(eq(category.getSlug()), any(Pageable.class));
    }

    @Test
    @DisplayName("updateProduct updates fields and translations")
    void updateProduct_success() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "Updated Olive Oil",
                "updated-olive-oil",
                "Updated description",
                new BigDecimal("279.90"),
                "https://example.com/updated.jpg",
                true,
                category.getSlug(),
                List.of(new LocalizedContentRequest("tr", "Guncel Zeytinyagi", "Guncel aciklama"))
        );
        ProductResponse updatedResponse = new ProductResponse(
                product.getId(),
                updateRequest.name(),
                updateRequest.slug(),
                updateRequest.description(),
                updateRequest.price(),
                updateRequest.imageUrl(),
                true,
                "tr",
                productResponse.category(),
                null,
                null
        );
        when(productRepository.findByIdWithDetails(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.existsBySlugAndIdNot(updateRequest.slug(), product.getId())).thenReturn(false);
        when(categoryRepository.findBySlug(category.getSlug())).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(eq(product), any())).thenReturn(updatedResponse);

        ProductResponse result = productService.updateProduct(product.getId(), updateRequest, "tr");

        assertThat(result).isEqualTo(updatedResponse);
        assertThat(product.getTranslations()).hasSize(1);
        verify(productMapper).updateEntity(updateRequest, product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("updateProduct rejects duplicate slug")
    void updateProduct_duplicateSlug() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null, "existing-product", null, null, null, null, null, null);
        when(productRepository.findByIdWithDetails(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.existsBySlugAndIdNot(updateRequest.slug(), product.getId())).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(product.getId(), updateRequest, "en"))
                .isInstanceOf(DuplicateSlugException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct rejects unknown category")
    void updateProduct_unknownCategory() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null, null, null, null, null, null, "missing-category", null);
        when(productRepository.findByIdWithDetails(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findBySlug(updateRequest.categorySlug())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(product.getId(), updateRequest, "en"))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct rejects unknown product")
    void updateProduct_notFound() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(null, null, null, null, null, null, null, null);
        when(productRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(404L, updateRequest, "en"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("deleteProduct deletes existing product")
    void deleteProduct_success() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        productService.deleteProduct(product.getId());

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("deleteProduct rejects unknown product")
    void deleteProduct_notFound() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(404L))
                .isInstanceOf(ProductNotFoundException.class);
    }

}
