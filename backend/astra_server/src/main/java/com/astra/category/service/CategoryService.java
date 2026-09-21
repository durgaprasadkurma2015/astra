package com.astra.category.service;

import com.astra.category.dto.CategoryRequest;
import com.astra.category.dto.CategoryResponse;
import com.astra.entity.Category;
import com.astra.exception.ApiException;
import com.astra.repository.CategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<CategoryResponse> getAll() {

        return repository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getById(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Category not found."
                        )
                );

        return toResponse(category);
    }

    public CategoryResponse create(CategoryRequest request) {

        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Category already exists."
            );
        }

        String slug = createSlug(request.name());

        Category category = Category.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description())
                .imageUrl(request.imageUrl())
                .active(request.active())
                .build();

        return toResponse(repository.save(category));
    }

    public CategoryResponse update(
            Long id,
            CategoryRequest request) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Category not found."
                        )
                );

        category.setName(request.name().trim());
        category.setSlug(createSlug(request.name()));
        category.setDescription(request.description());
        category.setImageUrl(request.imageUrl());
        category.setActive(request.active());

        return toResponse(repository.save(category));
    }

    public void delete(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Category not found."
                        )
                );

        category.setActive(false);

        repository.save(category);
    }

    private String createSlug(String value) {

        return value
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private CategoryResponse toResponse(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getImageUrl(),
                category.isActive()
        );
    }
}