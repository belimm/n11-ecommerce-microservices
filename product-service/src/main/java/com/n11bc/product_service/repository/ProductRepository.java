package com.n11bc.product_service.repository;

import com.n11bc.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategorySlugAndActiveTrue(String categorySlug, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByCategoryId(Long categoryId);
}
