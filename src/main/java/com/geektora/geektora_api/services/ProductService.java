package com.geektora.geektora_api.services;

import com.geektora.geektora_api.DTO.image.ImageResponseDTO;
import com.geektora.geektora_api.DTO.product.ProductCreateDTO;
import com.geektora.geektora_api.DTO.product.ProductResponseDTO;
import com.geektora.geektora_api.exceptions.ResourceNotExistsException;
import com.geektora.geektora_api.mappers.ProductMapper;
import com.geektora.geektora_api.model.entity.Category;
import com.geektora.geektora_api.model.entity.Image;
import com.geektora.geektora_api.model.entity.Product;
import com.geektora.geektora_api.model.entity.Tag;
import com.geektora.geektora_api.repository.article.CategoryRepository;
import com.geektora.geektora_api.repository.article.ImageRepository;
import com.geektora.geektora_api.repository.article.ProductRepository;
import com.geektora.geektora_api.repository.article.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductMapper productMapper;
    @Autowired private ImgurService imgurService;
    @Autowired private ImageRepository imageRepository;

    public ProductResponseDTO createProduct(ProductCreateDTO productDTO) {
        // Convertir el DTO en entidad Product
        Product product = productMapper.toEntity(productDTO);

        // Verificar si existen tags y asignarlos al producto
        if (productDTO.getTagIds() != null) {
            List<Tag> tags = productDTO.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotExistsException("Tag not found with id: " + tagId)))
                    .collect(Collectors.toList());
            product.setTags(tags);
        }

        if (productDTO.getCategoryIds() != null) {
            List<Category> categories = productDTO.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotExistsException("Category not found with id: " + categoryId)))
                    .collect(Collectors.toList());
            product.setCategories(categories);
        }

        // Guardar el producto inicialmente en la base de datos
        Product savedProduct = productRepository.save(product);

        // Verificar si se han proporcionado imágenes
        List<ImageResponseDTO> imageResponseDTOs = null;
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            // Subir imágenes y obtener URLs + deleteHashes
            List<ImageResponseDTO> uploadedImages = imgurService.uploadImages(productDTO.getImages());

            // Crear y asociar las URLs de las imágenes al producto
            List<Image> images = uploadedImages.stream()
                    .map(data -> {
                        Image image = new Image();
                        image.setUrl(data.getUrl());
                        image.setDeleteHash(data.getDeleteHash());
                        image.setActive(true);
                        image.setProduct(savedProduct);
                        return image;
                    })
                    .collect(Collectors.toList());

            // Guardar las imágenes en la base de datos
            imageRepository.saveAll(images);  // Guardar todas las imágenes en la base de datos

            // Mapear las imágenes a ImageResponseDTO
            imageResponseDTOs = images.stream()
                    .map(image -> {
                        ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
                        imageResponseDTO.setImageId(image.getIdImage());
                        imageResponseDTO.setActive(image.isActive());
                        imageResponseDTO.setUrl(image.getUrl());
                        imageResponseDTO.setDeleteHash(image.getDeleteHash());
                        return imageResponseDTO;
                    })
                    .collect(Collectors.toList());
        }

        // Crear el DTO de respuesta
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setName(savedProduct.getName());
        responseDTO.setDescription(savedProduct.getDescription());
        responseDTO.setPrice(savedProduct.getPrice());
        responseDTO.setStock(savedProduct.getStock());
        responseDTO.setCreatedAt(savedProduct.getCreatedAt());
        responseDTO.setTagIds(savedProduct.getTags().stream().map(Tag::getIdTag).collect(Collectors.toList()));
        responseDTO.setCategoryIds(savedProduct.getCategories().stream().map(Category::getIdCategory).collect(Collectors.toList()));
        responseDTO.setImages(imageResponseDTOs);

        // Retornar el DTO
        return responseDTO;
    }

    public ProductResponseDTO editProduct(int idProduct, ProductCreateDTO productDTO) {
        // Verificar si el producto existe
        Product existingProduct = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotExistsException("Product not found with id: " + idProduct));

        // Actualizar los atributos del producto
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());

        // Actualizar los tags
        if (productDTO.getTagIds() != null) {
            List<Tag> tags = productDTO.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotExistsException("Tag not found with id: " + tagId)))
                    .collect(Collectors.toList());
            existingProduct.setTags(tags);
        }

        // Actualizar las categorías
        if (productDTO.getCategoryIds() != null) {
            List<Category> categories = productDTO.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotExistsException("Category not found with id: " + categoryId)))
                    .collect(Collectors.toList());
            existingProduct.setCategories(categories);
        }

        List<ImageResponseDTO> imageResponseDTOs = null;
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            //obtendremos las imagenes relacionadas con el producto

            List<Image> image = imageRepository.findByProduct_IdProduct(idProduct);

            // Desactivar imágenes actuales individualmente
            for(Image imagesf : image){
                imagesf.setActive(false);
                imageRepository.save(imagesf);
            }

            // Subir las nuevas imágenes a Imgur y obtener URLs + deleteHashes
            List<ImageResponseDTO> uploadedImages = imgurService.uploadImages(productDTO.getImages());

            // Crear y asociar las nuevas imágenes al producto
            List<Image> images = uploadedImages.stream()
                    .map(data -> {
                        Image imageL = new Image();
                        imageL.setUrl(data.getUrl());
                        imageL.setDeleteHash(data.getDeleteHash());
                        imageL.setActive(true);
                        imageL.setProduct(existingProduct);
                        return imageL;
                    })
                    .collect(Collectors.toList());

            // Guardar las nuevas imágenes en la base de datos
            imageRepository.saveAll(images);

            // Mapear las imágenes a ImageResponseDTO
            imageResponseDTOs = images.stream()
                    .map(imageR -> {
                        ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
                        imageResponseDTO.setImageId(imageR.getIdImage());
                        imageResponseDTO.setActive(imageR.isActive());
                        imageResponseDTO.setUrl(imageR.getUrl());
                        imageResponseDTO.setDeleteHash(imageR.getDeleteHash());
                        return imageResponseDTO;
                    })
                    .collect(Collectors.toList());
        } else {
            // Si no se enviaron nuevas imágenes, mantener las imágenes activas existentes
            List<Image> image = imageRepository.findByProduct_IdProduct(idProduct);

            imageResponseDTOs = image.stream()
                    .filter(Image::isActive)
                    .map(imageR -> {
                        ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
                        imageResponseDTO.setImageId(imageR.getIdImage());
                        imageResponseDTO.setUrl(imageR.getUrl());
                        imageResponseDTO.setDeleteHash(imageR.getDeleteHash());
                        imageResponseDTO.setActive(imageR.isActive());
                        return imageResponseDTO;
                    })
                    .collect(Collectors.toList());
        }

        // Guardar el producto actualizado
        productRepository.save(existingProduct);

        // Crear el DTO de respuesta
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setName(existingProduct.getName());
        responseDTO.setDescription(existingProduct.getDescription());
        responseDTO.setPrice(existingProduct.getPrice());
        responseDTO.setStock(existingProduct.getStock());
        responseDTO.setCreatedAt(existingProduct.getCreatedAt());
        responseDTO.setTagIds(existingProduct.getTags().stream().map(Tag::getIdTag).collect(Collectors.toList()));
        responseDTO.setCategoryIds(existingProduct.getCategories().stream().map(Category::getIdCategory).collect(Collectors.toList()));
        responseDTO.setImages(imageResponseDTOs);

        return responseDTO;
    }

    public ResponseEntity<String> deleteProduct(int idProduct) {
        if(!productRepository.existsById(idProduct)) {
            throw new ResourceNotExistsException("Product not found with id: " + idProduct);
        }

        if(productRepository.findById(idProduct).get().getActive()) {
            return ResponseEntity.status(403).body("Product cannot be deleted because is active");
        }

        List<Image> images= imgurService.getImagebyIdProduct(idProduct);

        for (Image image : images) {
            imgurService.deleteImagebyId(image.getIdImage());
        }

        productRepository.deleteById(idProduct);
        return ResponseEntity.ok("You eliminated the product with id: " + idProduct);
    }

    public ResponseEntity<String> updateState(int idProduct) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + idProduct));

        product.setActive(!product.getActive());

        List<Image> images = imgurService.getImagebyIdProduct(idProduct);

        List<ImageResponseDTO> imageResponseDTOs = new ArrayList<>();

            if (product.getActive()) {
            for (Image image : images) {
                image.setActive(image.isLaststate());
                image.setLaststate(false);

                imageRepository.save(image);

                imageResponseDTOs.add(new ImageResponseDTO(
                        image.getIdImage(),
                        image.getUrl(),
                        image.getDeleteHash(),
                        image.isActive(),
                        image.isLaststate()
                ));
            }
        } else {
            for (Image image : images) {
                image.setLaststate(image.isActive());
                image.setActive(false);

                imageRepository.save(image);

                imageResponseDTOs.add(new ImageResponseDTO(
                        image.getIdImage(),
                        image.getUrl(),
                        image.getDeleteHash(),
                        image.isActive(),
                        image.isLaststate()
                ));
            }
        }

        productRepository.save(product);
        return ResponseEntity.status(200).body("change product state successfully");
    }
}