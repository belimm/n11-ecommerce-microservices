package com.n11bc.product_service.service;

import com.n11bc.product_service.dto.request.CategoryRequest;
import com.n11bc.product_service.dto.request.LocalizedContentRequest;
import com.n11bc.product_service.dto.response.CategoryResponse;
import com.n11bc.product_service.entity.Category;
import com.n11bc.product_service.exception.CategoryInUseException;
import com.n11bc.product_service.exception.CategoryNotFoundException;
import com.n11bc.product_service.exception.DuplicateSlugException;
import com.n11bc.product_service.mapper.CategoryMapper;
import com.n11bc.product_service.repository.CategoryRepository;
import com.n11bc.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Herbal Pantry")
                .slug("herbal-pantry")
                .description("Natural pantry staples")
                .build();
        request = new CategoryRequest(
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                List.of(new LocalizedContentRequest("tr", "Bitkisel Kiler", "Dogal temel mutfak urunleri"))
        );
        response = new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), "en", null, null);
    }

    @Test
    @DisplayName("createCategory creates category with translations")
    void createCategory_success() {
        when(categoryRepository.existsBySlug(request.slug())).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(eq(category), any())).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request, "tr");

        assertThat(result).isEqualTo(response);
        assertThat(category.getTranslations()).hasSize(1);
        assertThat(category.getTranslations().get(0).getLocale()).isEqualTo("tr");
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("createCategory rejects duplicate slug")
    void createCategory_duplicateSlug() {
        when(categoryRepository.existsBySlug(request.slug())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request, "en"))
                .isInstanceOf(DuplicateSlugException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCategory rejects category with products")
    void deleteCategory_inUse() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(category.getId())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(category.getId()))
                .isInstanceOf(CategoryInUseException.class);

        verify(categoryRepository, never()).delete(any());
    }


    @Test
    @DisplayName("getAllCategories returns localized category list")
    void getAllCategories_success() {
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(eq(category), any())).thenReturn(response);

        List<CategoryResponse> result = categoryService.getAllCategories("tr-TR");

        assertThat(result).containsExactly(response);
    }

    @Test
    @DisplayName("getCategoryById returns category detail")
    void getCategoryById_success() {
        when(categoryRepository.findByIdWithTranslations(category.getId())).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(eq(category), any())).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryById(category.getId(), "en");

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("getCategoryById rejects unknown category")
    void getCategoryById_notFound() {
        when(categoryRepository.findByIdWithTranslations(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(404L, "en"))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("updateCategory updates fields and translations")
    void updateCategory_success() {
        CategoryResponse updatedResponse = new CategoryResponse(category.getId(), "Updated Category", "updated-category", "Updated", "tr", null, null);
        CategoryRequest updateRequest = new CategoryRequest(
                "Updated Category",
                "updated-category",
                "Updated",
                List.of(new LocalizedContentRequest("tr", "Guncel Kategori", "Guncel aciklama"))
        );
        when(categoryRepository.findByIdWithTranslations(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugAndIdNot(updateRequest.slug(), category.getId())).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(eq(category), any())).thenReturn(updatedResponse);

        CategoryResponse result = categoryService.updateCategory(category.getId(), updateRequest, "tr");

        assertThat(result).isEqualTo(updatedResponse);
        assertThat(category.getTranslations()).hasSize(1);
        verify(categoryMapper).updateEntity(updateRequest, category);
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("updateCategory rejects duplicate slug")
    void updateCategory_duplicateSlug() {
        when(categoryRepository.findByIdWithTranslations(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugAndIdNot(request.slug(), category.getId())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(category.getId(), request, "en"))
                .isInstanceOf(DuplicateSlugException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCategory rejects unknown category")
    void updateCategory_notFound() {
        when(categoryRepository.findByIdWithTranslations(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(404L, request, "en"))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("deleteCategory deletes unused category")
    void deleteCategory_success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(category.getId())).thenReturn(false);

        categoryService.deleteCategory(category.getId());

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("deleteCategory rejects unknown category")
    void deleteCategory_notFound() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(404L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

}
