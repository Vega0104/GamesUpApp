package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Action");
        category.setSlug("action");
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_ShouldCreateCategory_WhenValidName() {
        // Arrange
        when(categoryRepository.existsByName("Strategy")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(2L);
            return cat;
        });

        // Act
        Category result = categoryService.create("Strategy");

        // Assert
        assertNotNull(result);
        assertEquals("Strategy", result.getName());
        assertEquals("strategy", result.getSlug());
        verify(categoryRepository).existsByName("Strategy");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_ShouldTrimName_WhenNameHasWhitespace() {
        // Arrange
        when(categoryRepository.existsByName("RPG")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Category result = categoryService.create("  RPG  ");

        // Assert
        assertEquals("RPG", result.getName());
        assertEquals("rpg", result.getSlug());
    }

    @Test
    void create_ShouldGenerateSlugWithAccents_WhenNameHasAccents() {
        // Arrange
        when(categoryRepository.existsByName("Stratégie")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Category result = categoryService.create("Stratégie");

        // Assert
        assertEquals("Stratégie", result.getName());
        assertEquals("strategie", result.getSlug());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.create(null)
        );
        assertEquals("Category name cannot be null or empty", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.create("   ")
        );
        assertEquals("Category name cannot be null or empty", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(categoryRepository.existsByName("Action")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.create("Action")
        );
        assertEquals("Category with name 'Action' already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnCategory_WhenIdExists() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        Optional<Category> result = categoryService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Action", result.get().getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Category> result = categoryService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(categoryRepository).findById(99L);
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.findById(null)
        );
        assertEquals("Invalid category ID", exception.getMessage());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.findById(0L)
        );
        assertEquals("Invalid category ID", exception.getMessage());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.findById(-1L)
        );
        assertEquals("Invalid category ID", exception.getMessage());
    }

    // ========== FIND BY SLUG TESTS ==========

    @Test
    void findBySlug_ShouldReturnCategory_WhenSlugExists() {
        // Arrange
        when(categoryRepository.findBySlug("action")).thenReturn(Optional.of(category));

        // Act
        Optional<Category> result = categoryService.findBySlug("action");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Action", result.get().getName());
        verify(categoryRepository).findBySlug("action");
    }

    @Test
    void findBySlug_ShouldReturnEmpty_WhenSlugDoesNotExist() {
        // Arrange
        when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        // Act
        Optional<Category> result = categoryService.findBySlug("unknown");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findBySlug_ShouldThrowException_WhenSlugIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.findBySlug(null)
        );
        assertEquals("Slug cannot be null or empty", exception.getMessage());
    }

    @Test
    void findBySlug_ShouldThrowException_WhenSlugIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.findBySlug("  ")
        );
        assertEquals("Slug cannot be null or empty", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllCategories() {
        // Arrange
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("RPG");
        category2.setSlug("rpg");

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category, category2));

        // Act
        List<Category> result = categoryService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Action", result.get(0).getName());
        assertEquals("RPG", result.get(1).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCategories() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Category> result = categoryService.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_ShouldUpdateCategory_WhenValidData() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Adventure")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Category result = categoryService.update(1L, "Adventure");

        // Assert
        assertEquals("Adventure", result.getName());
        assertEquals("adventure", result.getSlug());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category);
    }

    @Test
    void update_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.update(99L, "New Name")
        );
        assertEquals("Category not found with id: 99", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Strategy")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.update(1L, "Strategy")
        );
        assertEquals("Category with name 'Strategy' already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.update(null, "Name")
        );
        assertEquals("Invalid category ID", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.update(1L, null)
        );
        assertEquals("Category name cannot be null or empty", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeleteCategory_WhenIdExists() {
        // Arrange
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        // Act
        categoryService.delete(1L);

        // Assert
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(categoryRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.delete(99L)
        );
        assertEquals("Category not found with id: 99", exception.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.delete(null)
        );
        assertEquals("Invalid category ID", exception.getMessage());
    }
}