package com.geektora.geektora_api.repository.article;

import com.geektora.geektora_api.model.entity.Comment;
import com.geektora.geektora_api.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
