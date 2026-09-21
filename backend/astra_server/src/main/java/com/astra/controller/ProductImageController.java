package com.astra.controller;

import com.astra.dto.ProductImageRequest;
import com.astra.dto.ProductImageReorderRequest;
import com.astra.dto.ProductImageResponse;
import com.astra.service.ProductImageService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(
            ProductImageService productImageService
    ) {

        this.productImageService =
                productImageService;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductImageResponse>
    uploadImage(

            @PathVariable Long productId,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    value = "primaryImage",
                    defaultValue = "false"
            )
            boolean primaryImage

    ) {

        ProductImageResponse response =
                productImageService.uploadImage(
                        productId,
                        file,
                        primaryImage
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(
            value = "/url",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProductImageResponse>
    addImageUrl(

            @PathVariable Long productId,

            @Valid
            @RequestBody ProductImageRequest request

    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productImageService.addImageUrl(
                                productId,
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<ProductImageResponse>>
    getImages(

            @PathVariable Long productId

    ) {

        return ResponseEntity.ok(
                productImageService.getImages(
                        productId
                )
        );
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<ProductImageResponse>
    updateImage(

            @PathVariable Long productId,

            @PathVariable Long imageId,

            @Valid
            @RequestBody ProductImageRequest request

    ) {

        return ResponseEntity.ok(
                productImageService.updateImage(
                        productId,
                        imageId,
                        request
                )
        );
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void>
    deleteImage(

            @PathVariable Long productId,

            @PathVariable Long imageId

    ) {

        productImageService.deleteImage(
                productId,
                imageId
        );

        return ResponseEntity.noContent()
                .build();
    }

    @PutMapping("/{imageId}/primary")
    public ResponseEntity<ProductImageResponse>
    setPrimary(

            @PathVariable Long productId,

            @PathVariable Long imageId

    ) {

        return ResponseEntity.ok(
                productImageService.setPrimary(
                        productId,
                        imageId
                )
        );
    }

    @PutMapping("/reorder")
    public ResponseEntity<List<ProductImageResponse>>
    reorderImages(

            @PathVariable Long productId,

            @Valid
            @RequestBody ProductImageReorderRequest request

    ) {

        return ResponseEntity.ok(
                productImageService.reorderImages(
                        productId,
                        request
                )
        );
    }
}