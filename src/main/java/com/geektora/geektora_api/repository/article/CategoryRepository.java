package com.geektora.geektora_api.repository.article;

import com.geektora.geektora_api.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
