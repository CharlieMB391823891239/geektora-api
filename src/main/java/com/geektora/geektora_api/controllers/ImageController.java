package com.geektora.geektora_api.controllers;

import com.geektora.geektora_api.DTO.image.ImageResponseDTO;
import com.geektora.geektora_api.model.entity.Image;
import com.geektora.geektora_api.services.ImgurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImgurService imgurService;

    public ImageController(ImgurService imgurService) {
        this.imgurService = imgurService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<ImageResponseDTO>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            return ResponseEntity.ok(imgurService.uploadImages(files));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Image>> getImagesFromImgur() {
        try {
            List<Image> images = imgurService.getImages();
            return ResponseEntity.ok(images);  // Retornamos la lista de imágenes en el cuerpo de la respuesta
        } catch (Exception e) {
            // Imprime el error para poder rastrear el problema y devuelve un error 500
            System.err.println("Error obteniendo imágenes: " + e.getMessage());
            return ResponseEntity.status(500).body(null); // Cambié el 404 a 500 ya que es un error del servidor
        }
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<List<Image>> getImagesByProduct(@PathVariable int id) {
        List<Image> images = imgurService.getImagebyIdProduct(id);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/deleteImgProduct/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable int id) {
        try{
        Image image = imgurService.deleteImagebyId(id);
        return ResponseEntity.ok("Delete successfully image deleted: "+ image.getIdImage());
    } catch (RuntimeException e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
    }

    @DeleteMapping("/deleteImagesProduct")
    public ResponseEntity<String> deleteImages(@RequestBody List<Integer> ids) {
        StringBuilder response = new StringBuilder();

        for (Integer id : ids) {
            try {
                // Llamar al servicio para eliminar la imagen
                Image image = imgurService.deleteImagebyId(id);
                response.append("Successfully deleted image with ID: ").append(image.getIdImage()).append("\n");
            } catch (RuntimeException e) {
                // Si hay un error (por ejemplo, imagen no encontrada o activa), lo agregamos al response
                response.append("Error deleting image with ID: ").append(id).append(" - ").append(e.getMessage()).append("\n");
            }
        }

        if (response.toString().contains("Error")) {
            return ResponseEntity.status(500).body(response.toString());
        }
        return ResponseEntity.ok(response.toString());
    }

    @PatchMapping("updateState/{id}")
    public ResponseEntity<Image> updateStateImage(@PathVariable int id) {
        Image images = imgurService.updateState(id);
        return ResponseEntity.ok(images);
    }
}