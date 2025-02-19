package com.geektora.geektora_api.mappers;

import com.geektora.geektora_api.DTO.image.ImageResponseDTO;
import com.geektora.geektora_api.model.entity.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public ImageResponseDTO toDTO(Image image) {
        return new ImageResponseDTO(
                image.getIdImage(),
                image.getUrl(),
                image.getDeleteHash(),
                image.isActive(),
                image.isLaststate()
        );
    }
}