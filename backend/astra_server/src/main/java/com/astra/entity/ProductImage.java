package com.astra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(
                        name = "idx_product_image_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_product_image_primary",
                        columnList = "product_id, primary_image"
                ),
                @Index(
                        name = "idx_product_image_order",
                        columnList = "product_id, display_order"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_image_product")
    )
    private Product product;

    @Column(
            name = "image_url",
            nullable = false,
            length = 1000
    )
    private String imageUrl;

    @Column(
            name = "original_file_name",
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "stored_file_name",
            length = 255
    )
    private String storedFileName;

    @Column(
            name = "content_type",
            length = 100
    )
    private String contentType;

    @Column(
            name = "file_size"
    )
    private Long fileSize;

    @Column(
            name = "primary_image",
            nullable = false
    )
    private boolean primaryImage;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;
}