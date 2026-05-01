package com.n11bc.product_service.service;

import com.n11bc.product_service.dto.request.LocalizedContentRequest;
import com.n11bc.product_service.dto.request.ProductCreateRequest;
import com.n11bc.product_service.dto.request.ProductUpdateRequest;
import com.n11bc.product_service.dto.response.PageResponse;
import com.n11bc.product_service.dto.response.ProductResponse;
import com.n11bc.product_service.dto.response.ProductSummaryResponse;
import com.n11bc.product_service.entity.Category;
import com.n11bc.product_service.entity.Product;
import com.n11bc.product_service.entity.ProductTranslation;
import com.n11bc.product_service.exception.CategoryNotFoundException;
import com.n11bc.product_service.exception.DuplicateSlugException;
import com.n11bc.product_service.exception.ProductNotFoundException;
import com.n11bc.product_service.mapper.LocaleContext;
import com.n11bc.product_service.mapper.ProductMapper;
import com.n11bc.product_service.repository.CategoryRepository;
import com.n11bc.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public PageResponse<ProductSummaryResponse> getProducts(int page, int size, String categorySlug, String acceptLanguage) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizePageSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> products = hasText(categorySlug)
                ? productRepository.findByCategorySlugAndActiveTrue(categorySlug, pageable)
                : productRepository.findByActiveTrue(pageable);
        LocaleContext localeContext = LocaleContext.fromAcceptLanguage(acceptLanguage);
        List<ProductSummaryResponse> items = products.getContent().stream()
                .map(product -> productMapper.toSummaryResponse(product, localeContext))
                .toList();
        return new PageResponse<>(items, products.getNumber(), products.getSize(), products.getTotalElements(), products.getTotalPages(), products.isLast());
    }

    @Override
    public ProductResponse getProductById(Long id, String acceptLanguage) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponse(product, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    public ProductResponse getProductBySlug(String slug, String acceptLanguage) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException(slug));
        return productMapper.toResponse(product, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String acceptLanguage) {
        assertSlugAvailable(request.slug());
        Category category = categoryRepository.findBySlug(request.categorySlug())
                .orElseThrow(() -> new CategoryNotFoundException(request.categorySlug()));

        Product product = productMapper.toEntity(request);
        product.setActive(request.active() == null || request.active());
        product.setCategory(category);
        replaceTranslations(product, request.translations());

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}", savedProduct.getSlug());
        return productMapper.toResponse(savedProduct, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, String acceptLanguage) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (hasText(request.slug()) && productRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new DuplicateSlugException("Product", request.slug());
        }

        productMapper.updateEntity(request, product);
        if (hasText(request.categorySlug())) {
            Category category = categoryRepository.findBySlug(request.categorySlug())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categorySlug()));
            product.setCategory(category);
        }
        if (request.translations() != null) {
            replaceTranslations(product, request.translations());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product updated: {}", savedProduct.getSlug());
        return productMapper.toResponse(savedProduct, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.delete(product);
        log.info("Product deleted: {}", product.getSlug());
    }

    private void assertSlugAvailable(String slug) {
        if (productRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException("Product", slug);
        }
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 12;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private void replaceTranslations(Product product, List<LocalizedContentRequest> translations) {
        product.getTranslations().clear();
        if (translations == null) {
            return;
        }
        translations.stream()
                .map(request -> ProductTranslation.builder()
                        .locale(normalizeLocale(request.locale()))
                        .name(request.name())
                        .description(request.description())
                        .searchText(buildSearchText(request))
                        .build())
                .forEach(product::addTranslation);
    }

    private String buildSearchText(LocalizedContentRequest request) {
        return (request.name() + " " + (request.description() == null ? "" : request.description())).trim();
    }

    private String normalizeLocale(String locale) {
        return locale.trim().replace('_', '-').toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
