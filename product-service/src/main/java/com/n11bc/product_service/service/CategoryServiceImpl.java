package com.n11bc.product_service.service;

import com.n11bc.product_service.dto.request.CategoryRequest;
import com.n11bc.product_service.dto.request.LocalizedContentRequest;
import com.n11bc.product_service.dto.response.CategoryResponse;
import com.n11bc.product_service.entity.Category;
import com.n11bc.product_service.entity.CategoryTranslation;
import com.n11bc.product_service.exception.CategoryInUseException;
import com.n11bc.product_service.exception.CategoryNotFoundException;
import com.n11bc.product_service.exception.DuplicateSlugException;
import com.n11bc.product_service.mapper.CategoryMapper;
import com.n11bc.product_service.mapper.LocaleContext;
import com.n11bc.product_service.repository.CategoryRepository;
import com.n11bc.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories(String acceptLanguage) {
        LocaleContext localeContext = LocaleContext.fromAcceptLanguage(acceptLanguage);
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> categoryMapper.toResponse(category, localeContext))
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id, String acceptLanguage) {
        Category category = categoryRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return categoryMapper.toResponse(category, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String acceptLanguage) {
        assertSlugAvailable(request.slug());
        Category category = categoryMapper.toEntity(request);
        replaceTranslations(category, request.translations());
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: {}", savedCategory.getSlug());
        return categoryMapper.toResponse(savedCategory, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, String acceptLanguage) {
        Category category = categoryRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new DuplicateSlugException("Category", request.slug());
        }

        categoryMapper.updateEntity(request, category);
        if (request.translations() != null) {
            replaceTranslations(category, request.translations());
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category updated: {}", savedCategory.getSlug());
        return categoryMapper.toResponse(savedCategory, LocaleContext.fromAcceptLanguage(acceptLanguage));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(id);
        }
        categoryRepository.delete(category);
        log.info("Category deleted: {}", category.getSlug());
    }

    private void assertSlugAvailable(String slug) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException("Category", slug);
        }
    }

    private void replaceTranslations(Category category, List<LocalizedContentRequest> translations) {
        category.getTranslations().clear();
        if (translations == null) {
            return;
        }
        translations.stream()
                .map(request -> CategoryTranslation.builder()
                        .locale(normalizeLocale(request.locale()))
                        .name(request.name())
                        .description(request.description())
                        .build())
                .forEach(category::addTranslation);
    }

    private String normalizeLocale(String locale) {
        return locale.trim().replace('_', '-').toLowerCase(Locale.ROOT);
    }
}
