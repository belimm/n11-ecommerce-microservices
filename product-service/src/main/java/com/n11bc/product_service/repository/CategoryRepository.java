package com.n11bc.product_service.repository;

import com.n11bc.product_service.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = "translations")
    List<Category> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "translations")
    Optional<Category> findBySlug(String slug);

    @EntityGraph(attributePaths = "translations")
    @Query("select c from Category c where c.id = :id")
    Optional<Category> findByIdWithTranslations(@Param("id") Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
