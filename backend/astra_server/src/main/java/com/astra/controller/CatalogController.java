package com.astra.controller;

import com.astra.category.dto.CategoryResponse;
import com.astra.category.service.CategoryService;
import com.astra.product.dto.ProductResponse;
import com.astra.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public CatalogController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return categoryService.getAll();
    }

    @GetMapping("/featured")
    public Page<ProductResponse> featured(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.featured(page, size);
    }
}
