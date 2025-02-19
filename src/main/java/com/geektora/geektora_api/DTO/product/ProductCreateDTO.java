package com.geektora.geektora_api.DTO.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@Data
public class ProductCreateDTO {
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private List<Integer> tagIds;
    private List<Integer> categoryIds;
    private List<MultipartFile> images;
}