package com.geektora.geektora_api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geektora.geektora_api.DTO.image.ImageResponseDTO;
import com.geektora.geektora_api.model.entity.Image;
import com.geektora.geektora_api.repository.article.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ImgurService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String API_URL = "https://api.imgur.com/3/account/me/images";
    @Autowired
    private ImageRepository imageRepository;

    @Value("${imgur.api.url}") // ✅ Cargar la URL desde application.properties
    private String imgurApiUrl;

    @Value("${imgur.bearer.token}") // ✅ Token de acceso de Imgur
    private String bearerToken;

    public ImgurService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<ImageResponseDTO> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(image -> {
                    try {
                        return uploadImage(image);
                    } catch (Exception e) {
                        System.err.println("Error subiendo imagen: " + e.getMessage());
                        return null;
                    }
                })
                .filter(img -> img != null)
                .map(img -> new ImageResponseDTO(null, img.get("url"), img.get("deleteHash"), true,false))
                .collect(Collectors.toList());
    }

    private Map<String, String> uploadImage(MultipartFile image) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", image.getResource());
        body.add("type", "image");

        Mono<String> responseMono = webClient.post()
                .uri(imgurApiUrl)
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);

        String responseBody = responseMono.block();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        if (jsonNode.path("success").asBoolean()) {
            String url = jsonNode.path("data").path("link").asText();
            String deleteHash = jsonNode.path("data").path("deletehash").asText();

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("deleteHash", deleteHash);
            return result;
        } else {
            throw new RuntimeException("Error en Imgur: " + jsonNode.path("data").path("error").asText());
        }
    }

    public List<Image> getImages() {
        return imageRepository.findAll();
    }

    public List<Image> getImagebyIdProduct(int id) {
        return imageRepository.findByProduct_IdProduct(id);
    }

    public Image deleteImagebyId(int id) {
        Image image = imageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Image not found with ID: " + id));

        if(image.isActive()){
            throw new RuntimeException("You can't delete an active image");
        }

        // Eliminar la imagen y retornarla
        deleteImageFromImgur(image.getDeleteHash());
        
        imageRepository.delete(image);
        return image;
    }

    private void deleteImageFromImgur(String deleteHash) {
        if (deleteHash == null || deleteHash.isEmpty()) {
            throw new RuntimeException("Invalid delete hash.");
        }

        WebClient webClient = WebClient.create("https://api.imgur.com");

        webClient.delete()
                .uri("/3/image/{deleteHash}", deleteHash)  // Asegúrate de que deleteHash esté correcto
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> System.out.println("Delete request to Imgur completed"))
                .block();  // Usamos block() para esperar la respuesta de la solicitud sin usar async
    }

    public Image updateState(int id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + id));
        image.setActive(!image.isActive());
        imageRepository.save(image);
        return image;
    }
}