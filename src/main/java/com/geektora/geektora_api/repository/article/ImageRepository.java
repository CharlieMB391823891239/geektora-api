package com.geektora.geektora_api.repository.article;

import com.geektora.geektora_api.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {

    List<Image> findByProduct_IdProduct(Integer productId);
}
