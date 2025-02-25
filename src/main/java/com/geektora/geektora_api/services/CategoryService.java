package com.geektora.geektora_api.services;

import com.geektora.geektora_api.model.entity.Category;
import com.geektora.geektora_api.repository.article.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CategoryService {

    @Autowired CategoryRepository categoryRepository;

    public Optional<Category> findById(int id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> updateCategory(int id, String nameCategory, String description, Boolean active) {
        return categoryRepository.findById(id).map(category -> {
            if (nameCategory != null) category.setNameCategory(nameCategory);
            if (description != null) category.setDescription(description);
            if (active != null) category.setActive(active);

            return categoryRepository.save(category);
        });
    }

    @Transactional
    public ResponseEntity<Void> deleteCategory(int id) {

        if (categoryRepository.findById(id).get().getActive()) {
            return ResponseEntity.status(500).body(null);
        }
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoryRepository.deleteCategoryAssociations(id); // Eliminar relaciones
        categoryRepository.deleteById(id); // Eliminar la categoría
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Category> addCategory(String nameCategory, String descriptionCategory, Boolean active) {

        Category category = new Category();
        category.setNameCategory(nameCategory);
        category.setDescription(descriptionCategory);
        category.setActive(active);
        categoryRepository.save(category);

        return ResponseEntity.ok(category);
    }
}
