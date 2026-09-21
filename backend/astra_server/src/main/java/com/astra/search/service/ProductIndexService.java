package com.astra.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astra.entity.Product;
import com.astra.repository.ProductRepository;
import com.astra.search.document.ProductSearchDocument;
import com.astra.search.exception.SearchException;
import com.astra.search.mapper.ProductSearchMapper;
import com.astra.search.repository.ProductSearchRepository;

@Service
public class ProductIndexService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ProductSearchMapper productSearchMapper;

    public ProductIndexService(
            ProductRepository productRepository,
            ProductSearchRepository productSearchRepository,
            ProductSearchMapper productSearchMapper
    ) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
        this.productSearchMapper = productSearchMapper;
    }

    @Transactional(readOnly = true)
    public void indexProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new SearchException(
                                "Product not found: " + productId
                        )
                );

        ProductSearchDocument document =
                productSearchMapper.toDocument(product);

        productSearchRepository.save(document);
    }

    public void deleteProduct(Long productId) {

        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID cannot be null"
            );
        }

        productSearchRepository.deleteById(productId);
    }

    @Transactional(readOnly = true)
    public IndexResult reindexAllProducts() {

        List<Product> products = productRepository.findAll();

        long indexed = 0;
        long failed = 0;

        for (Product product : products) {

            try {

                ProductSearchDocument document =
                        productSearchMapper.toDocument(product);

                productSearchRepository.save(document);

                indexed++;

            } catch (Exception ex) {

                failed++;
            }
        }

        return new IndexResult(
                indexed,
                failed,
                products.size()
        );
    }

    public record IndexResult(
            long indexed,
            long failed,
            long total
    ) {
    }
}