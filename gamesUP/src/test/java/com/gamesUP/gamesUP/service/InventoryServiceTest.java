package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Inventory;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.repository.InventoryRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Game game;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");
        game.setSlug("test-game");
        game.setBasePrice(new BigDecimal("29.99"));
        game.setCurrency("EUR");

        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProduct(game);
        inventory.setSku("TEST-GAME-PC-001");
        inventory.setBasePrice(new BigDecimal("29.99"));
        inventory.setCurrency("EUR");
        inventory.setStockQuantity(10);
        inventory.setActive(true);
    }

    // ========== CREATE TESTS ==========









    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnItem_WhenIdExists() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        // Act
        Optional<Inventory> result = inventoryService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TEST-GAME-PC-001", result.get().getSku());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Inventory> result = inventoryService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.findById(null)
        );
        assertEquals("Invalid inventory ID", exception.getMessage());
    }

    // ========== FIND BY SKU TESTS ==========

    @Test
    void findBySku_ShouldReturnItem_WhenSkuExists() {
        // Arrange
        when(inventoryRepository.findBySku("TEST-GAME-PC-001")).thenReturn(Optional.of(inventory));

        // Act
        Optional<Inventory> result = inventoryService.findBySku("TEST-GAME-PC-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(10, result.get().getStockQuantity());
    }

    @Test
    void findBySku_ShouldThrowException_WhenSkuIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.findBySku(null)
        );
        assertEquals("SKU cannot be null or empty", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllItems() {
        // Arrange
        Inventory item2 = new Inventory();
        item2.setId(2L);
        when(inventoryRepository.findAll()).thenReturn(Arrays.asList(inventory, item2));

        // Act
        List<Inventory> result = inventoryService.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    // ========== FIND BY GAME TESTS ==========

    @Test
    void findByGame_ShouldReturnItems_WhenGameExists() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(inventoryRepository.findByProduct(game)).thenReturn(Arrays.asList(inventory));

        // Act
        List<Inventory> result = inventoryService.findByGame(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByGame_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.findByGame(99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void findByGame_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.findByGame(null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    // ========== FIND ACTIVE ITEMS TESTS ==========

    @Test
    void findActiveItems_ShouldReturnActiveItems() {
        // Arrange
        when(inventoryRepository.findByActiveTrue()).thenReturn(Arrays.asList(inventory));

        // Act
        List<Inventory> result = inventoryService.findActiveItems();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
    }

    // ========== FIND ITEMS WITH STOCK TESTS ==========

    @Test
    void findItemsWithStock_ShouldReturnItemsWithStock() {
        // Arrange
        when(inventoryRepository.findByStockQuantityGreaterThan(0))
                .thenReturn(Arrays.asList(inventory));

        // Act
        List<Inventory> result = inventoryService.findItemsWithStock();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getStockQuantity() > 0);
    }

    // ========== UPDATE STOCK TESTS ==========

    @Test
    void updateStock_ShouldUpdateStock_WhenValidData() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.updateStock(1L, 50);

        // Assert
        assertEquals(50, result.getStockQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void updateStock_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.updateStock(99L, 10)
        );
        assertEquals("Inventory not found with id: 99", exception.getMessage());
    }

    @Test
    void updateStock_ShouldThrowException_WhenStockQuantityIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.updateStock(1L, -5)
        );
        assertEquals("Stock quantity must be greater than or equal to 0", exception.getMessage());
    }

    // ========== INCREMENT STOCK TESTS ==========

    @Test
    void incrementStock_ShouldIncrementStock_WhenValidData() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.incrementStock(1L, 5);

        // Assert
        assertEquals(15, result.getStockQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void incrementStock_ShouldThrowException_WhenQuantityIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.incrementStock(1L, 0)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    // ========== DECREMENT STOCK TESTS ==========

    @Test
    void decrementStock_ShouldDecrementStock_WhenSufficientStock() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.decrementStock(1L, 3);

        // Assert
        assertEquals(7, result.getStockQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void decrementStock_ShouldThrowException_WhenInsufficientStock() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.decrementStock(1L, 20)
        );
        assertEquals("Insufficient stock. Available: 10, requested: 20", exception.getMessage());
    }

    @Test
    void decrementStock_ShouldThrowException_WhenQuantityIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.decrementStock(1L, 0)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    // ========== UPDATE PRICE TESTS ==========

    @Test
    void updatePrice_ShouldUpdatePrice_WhenValidData() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.updatePrice(1L, new BigDecimal("49.99"));

        // Assert
        assertEquals(new BigDecimal("49.99"), result.getBasePrice());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void updatePrice_ShouldThrowException_WhenPriceIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.updatePrice(1L, new BigDecimal("-10"))
        );
        assertEquals("Price must be greater than or equal to 0", exception.getMessage());
    }

    // ========== ACTIVATE TESTS ==========

    @Test
    void activate_ShouldActivateItem_WhenItemExists() {
        // Arrange
        inventory.setActive(false);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.activate(1L);

        // Assert
        assertTrue(result.getActive());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void activate_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.activate(99L)
        );
        assertEquals("Inventory not found with id: 99", exception.getMessage());
    }

    // ========== DEACTIVATE TESTS ==========

    @Test
    void deactivate_ShouldDeactivateItem_WhenItemExists() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.deactivate(1L);

        // Assert
        assertFalse(result.getActive());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void deactivate_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.deactivate(99L)
        );
        assertEquals("Inventory not found with id: 99", exception.getMessage());
    }

    // ========== HAS STOCK TESTS ==========

    @Test
    void hasStock_ShouldReturnTrue_WhenStockAvailable() {
        // Arrange
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        // Act
        boolean result = inventoryService.hasStock(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasStock_ShouldReturnFalse_WhenNoStock() {
        // Arrange
        inventory.setStockQuantity(0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        // Act
        boolean result = inventoryService.hasStock(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasStock_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.hasStock(99L)
        );
        assertEquals("Inventory not found with id: 99", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeleteItem_WhenIdExists() {
        // Arrange
        when(inventoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(inventoryRepository).deleteById(1L);

        // Act
        inventoryService.delete(1L);

        // Assert
        verify(inventoryRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(inventoryRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.delete(99L)
        );
        assertEquals("Inventory not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.delete(null)
        );
        assertEquals("Invalid inventory ID", exception.getMessage());
    }
}