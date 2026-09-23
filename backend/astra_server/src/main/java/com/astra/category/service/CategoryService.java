package com.astra.category.service;

import com.astra.category.dto.CategoryRequest;
import com.astra.category.dto.CategoryResponse;
import com.astra.entity.Category;
import com.astra.exception.ApiException;
import com.astra.repository.CategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {

        return repository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
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

        String name = normalizeRequired(request.name());

        if (repository.existsByNameIgnoreCase(name)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Category already exists."
            );
        }

        String slug = createSlug(name);

        if (slug.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Category name cannot produce a valid slug."
            );
        }

        if (repository.existsBySlug(slug)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Category slug already exists."
            );
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(normalizeOptional(request.description()))
                .imageUrl(normalizeOptional(request.imageUrl()))
                .active(request.active())
                .build();

        return toResponse(repository.save(category));
    }

    public CategoryResponse update(
            Long id,
            CategoryRequest request
    ) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Category not found."
                        )
                );

        String name = normalizeRequired(request.name());

        /*
         * Check duplicate name only when the name
         * is actually changing.
         */
        if (!name.equalsIgnoreCase(category.getName()) &&
                repository.existsByNameIgnoreCase(name)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Category already exists."
            );
        }

        String slug = createSlug(name);

        if (slug.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Category name cannot produce a valid slug."
            );
        }

        /*
         * Check duplicate slug only when another
         * category already owns it.
         */
        if (!slug.equals(category.getSlug()) &&
                repository.existsBySlug(slug)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Category slug already exists."
            );
        }

        category.setName(name);
        category.setSlug(slug);
        category.setDescription(
                normalizeOptional(request.description())
        );
        category.setImageUrl(
                normalizeOptional(request.imageUrl())
        );
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

        /*
         * Soft delete.
         * We keep the database record because products
         * may still reference this category.
         */
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

    private String normalizeRequired(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
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
