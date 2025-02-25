package com.geektora.geektora_api.repository.article;

import com.geektora.geektora_api.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Modifying
    @Query(value = "DELETE FROM product_category WHERE id_category = :categoryId", nativeQuery = true)
    void deleteCategoryAssociations(@Param("categoryId") int categoryId);
}
