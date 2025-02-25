package com.geektora.geektora_api.controllers;

import com.geektora.geektora_api.model.entity.Category;
import com.geektora.geektora_api.repository.article.CategoryRepository;
import com.geektora.geektora_api.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/All")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable int id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/addCategory")
    public ResponseEntity<Category> addCategoryControl(@RequestParam String nameCategory,
                                                @RequestParam String descriptionCategory,
                                                @RequestParam Boolean active) {
        return categoryService.addCategory(nameCategory,descriptionCategory,active);
    }

    @PatchMapping("/change/{id}")
    public ResponseEntity<Category> updateCategoryById(
            @PathVariable int id,
            @RequestParam(required = false) String nameCategory,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean active) {

        return categoryService.updateCategory(id, nameCategory, description, active)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable int id) {
        return categoryService.deleteCategory(id);
    }
}
