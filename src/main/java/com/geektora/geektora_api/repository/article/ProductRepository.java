package com.geektora.geektora_api.repository.article;

import com.geektora.geektora_api.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.geektora.geektora_api.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategories_IdCategoryAndTags_IdTag(int idCategory, int idTag, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.categories c " +
            "LEFT JOIN p.tags t " +
            "WHERE (:categoryIds IS NULL OR c.idCategory IN :categoryIds) " +
            "AND (:tagIds IS NULL OR t.idTag IN :tagIds)")
    Page<Product> findByCategoriesAndTags(
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("tagIds") List<Integer> tagIds,
            Pageable pageable
    );
}
