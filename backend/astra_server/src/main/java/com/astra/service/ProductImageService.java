package com.astra.service;

import com.astra.dto.ProductImageRequest;
import com.astra.dto.ProductImageReorderRequest;
import com.astra.dto.ProductImageResponse;
import com.astra.entity.Product;
import com.astra.entity.ProductImage;
import com.astra.repository.ProductImageRepository;
import com.astra.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;

    private final ProductRepository productRepository;

    private final MediaStorageService mediaStorageService;

    public ProductImageService(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository,
            MediaStorageService mediaStorageService
    ) {
        this.productImageRepository =
                productImageRepository;

        this.productRepository =
                productRepository;

        this.mediaStorageService =
                mediaStorageService;
    }

    @Transactional
    public ProductImageResponse uploadImage(
            Long productId,
            MultipartFile file,
            boolean primaryImage
    ) {

        Product product =
                getProduct(productId);

        String folder =
                "products/" + productId;

        MediaStorageService.StoredFile storedFile =
                mediaStorageService.store(
                        file,
                        folder
                );

        List<ProductImage> existingImages =
                productImageRepository
                        .findByProductIdOrderByDisplayOrderAsc(
                                productId
                        );

        int displayOrder =
                existingImages.size();

        boolean makePrimary =
                primaryImage ||
                        existingImages.isEmpty();

        if (makePrimary) {

            clearPrimaryImage(productId);
        }

        ProductImage image =
                ProductImage.builder()
                        .product(product)
                        .imageUrl(
                                buildImageUrl(
                                        productId,
                                        storedFile.storedFileName()
                                )
                        )
                        .originalFileName(
                                storedFile.originalFileName()
                        )
                        .storedFileName(
                                storedFile.storedFileName()
                        )
                        .contentType(
                                storedFile.contentType()
                        )
                        .fileSize(
                                storedFile.fileSize()
                        )
                        .primaryImage(
                                makePrimary
                        )
                        .displayOrder(
                                displayOrder
                        )
                        .build();

        ProductImage saved =
                productImageRepository.save(image);

        return toResponse(saved);
    }

    @Transactional
    public ProductImageResponse addImageUrl(
            Long productId,
            ProductImageRequest request
    ) {

        Product product =
                getProduct(productId);

        List<ProductImage> existingImages =
                productImageRepository
                        .findByProductIdOrderByDisplayOrderAsc(
                                productId
                        );

        int displayOrder =
                request.displayOrder() != null
                        ? request.displayOrder()
                        : existingImages.size();

        boolean makePrimary =
                request.primaryImage() ||
                        existingImages.isEmpty();

        if (makePrimary) {

            clearPrimaryImage(productId);
        }

        ProductImage image =
                ProductImage.builder()
                        .product(product)
                        .imageUrl(
                                request.imageUrl()
                        )
                        .primaryImage(
                                makePrimary
                        )
                        .displayOrder(
                                displayOrder
                        )
                        .build();

        ProductImage saved =
                productImageRepository.save(image);

        return toResponse(saved);
    }

    @Transactional
    public List<ProductImageResponse> getImages(
            Long productId
    ) {

        getProduct(productId);

        return productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(
                        productId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductImageResponse updateImage(
            Long productId,
            Long imageId,
            ProductImageRequest request
    ) {

        ProductImage image =
                getImage(
                        productId,
                        imageId
                );

        image.setImageUrl(
                request.imageUrl()
        );

        if (request.displayOrder() != null) {

            image.setDisplayOrder(
                    request.displayOrder()
            );
        }

        if (request.primaryImage()) {

            clearPrimaryImage(
                    productId
            );

            image.setPrimaryImage(true);

        } else {

            image.setPrimaryImage(false);
        }

        return toResponse(
                productImageRepository.save(image)
        );
    }

    @Transactional
    public void deleteImage(
            Long productId,
            Long imageId
    ) {

        ProductImage image =
                getImage(
                        productId,
                        imageId
                );

        boolean wasPrimary =
                image.isPrimaryImage();

        String storedFileName =
                image.getStoredFileName();

        productImageRepository.delete(image);

        if (storedFileName != null &&
                !storedFileName.isBlank()) {

            mediaStorageService.delete(
                    "products/" + productId,
                    storedFileName
            );
        }

        if (wasPrimary) {

            setFirstImageAsPrimary(
                    productId
            );
        }
    }

    @Transactional
    public ProductImageResponse setPrimary(
            Long productId,
            Long imageId
    ) {

        ProductImage image =
                getImage(
                        productId,
                        imageId
                );

        clearPrimaryImage(productId);

        image.setPrimaryImage(true);

        return toResponse(
                productImageRepository.save(image)
        );
    }

    @Transactional
    public List<ProductImageResponse> reorderImages(
            Long productId,
            ProductImageReorderRequest request
    ) {

        getProduct(productId);

        List<ProductImage> images =
                productImageRepository
                        .findByProductIdOrderByDisplayOrderAsc(
                                productId
                        );

        List<Long> requestedIds =
                request.imageIds();

        if (requestedIds.size()
                != images.size()) {

            throw new IllegalArgumentException(
                    "All product image IDs must be included"
            );
        }

        List<Long> existingIds =
                images.stream()
                        .map(ProductImage::getId)
                        .toList();

        if (!existingIds.containsAll(
                requestedIds
        ) || !requestedIds.containsAll(
                existingIds
        )) {

            throw new IllegalArgumentException(
                    "Image IDs do not belong to this product"
            );
        }

        for (int index = 0;
             index < requestedIds.size();
             index++) {

            Long imageId =
                    requestedIds.get(index);

            ProductImage image =
                    productImageRepository
                            .findByIdAndProductId(
                                    imageId,
                                    productId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Image not found: "
                                                    + imageId
                                    )
                            );

            image.setDisplayOrder(index);

            productImageRepository.save(image);
        }

        return getImages(productId);
    }

    private void clearPrimaryImage(
            Long productId
    ) {

        productImageRepository
                .findByProductIdAndPrimaryImageTrue(
                        productId
                )
                .ifPresent(image -> {

                    image.setPrimaryImage(false);

                    productImageRepository.save(
                            image
                    );
                });
    }

    private void setFirstImageAsPrimary(
            Long productId
    ) {

        List<ProductImage> images =
                productImageRepository
                        .findByProductIdOrderByDisplayOrderAsc(
                                productId
                        );

        if (!images.isEmpty()) {

            ProductImage first =
                    images.get(0);

            first.setPrimaryImage(true);

            productImageRepository.save(
                    first
            );
        }
    }

    private Product getProduct(
            Long productId
    ) {

        return productRepository
                .findById(productId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Product not found: "
                                        + productId
                        )
                );
    }

    private ProductImage getImage(
            Long productId,
            Long imageId
    ) {

        return productImageRepository
                .findByIdAndProductId(
                        imageId,
                        productId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Product image not found: "
                                        + imageId
                        )
                );
    }

    private ProductImageResponse toResponse(
            ProductImage image
    ) {

        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getImageUrl(),
                image.getOriginalFileName(),
                image.getStoredFileName(),
                image.getContentType(),
                image.getFileSize(),
                image.isPrimaryImage(),
                image.getDisplayOrder()
        );
    }

    private String buildImageUrl(
            Long productId,
            String fileName
    ) {

        return "/uploads/products/"
                + productId
                + "/"
                + fileName;
    }
}