package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        String trimmedName = name.trim();

        if (categoryRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
        }

        Category category = new Category();
        category.setName(trimmedName);
        category.setSlug(generateSlug(trimmedName));

        return categoryRepository.save(category);
    }

    public Optional<Category> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        return categoryRepository.findById(id);
    }

    public Optional<Category> findBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }
        return categoryRepository.findBySlug(slug.trim());
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category update(Long id, String name) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        String trimmedName = name.trim();

        if (!category.getName().equals(trimmedName) && categoryRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
        }

        category.setName(trimmedName);
        category.setSlug(generateSlug(trimmedName));

        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid category ID");
        }

        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }

        categoryRepository.deleteById(id);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ïî]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}