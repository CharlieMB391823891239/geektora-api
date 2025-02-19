package com.geektora.geektora_api.DTO.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ImageResponseDTO {
    private Integer imageId;
    private String url;
    private String deleteHash;
    private boolean active;
    private boolean laststate;
}
