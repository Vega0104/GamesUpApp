package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.*;
import com.gamesUP.gamesUP.repository.*;
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
class PurchaseLineServiceTest {

    @Mock
    private PurchaseLineRepository purchaseLineRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseLineService purchaseLineService;

    private User user;
    private Game game;
    private Purchase purchase;
    private PurchaseLine purchaseLine;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.CUSTOMER);

        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");
        game.setSlug("test-game");
        game.setBasePrice(new BigDecimal("29.99"));
        game.setCurrency("EUR");

        purchase = new Purchase();
        purchase.setId(1L);
        purchase.setPlacedBy(user);
        purchase.setStatus(OrderStatus.PENDING);
        purchase.setTotalAmount(BigDecimal.ZERO);
        purchase.setCurrency("EUR");

        purchaseLine = new PurchaseLine();
        purchaseLine.setId(1L);
        purchaseLine.setPurchase(purchase);
        purchaseLine.setItem(game);
        purchaseLine.setQuantity(2);
        purchaseLine.setUnitPriceAtPurchase(new BigDecimal("29.99"));
        purchaseLine.setCurrency("EUR");
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_ShouldCreatePurchaseLine_WhenValidData() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(purchaseLineRepository.save(any(PurchaseLine.class))).thenAnswer(invocation -> {
            PurchaseLine line = invocation.getArgument(0);
            line.setId(2L);
            return line;
        });

        // Act
        PurchaseLine result = purchaseLineService.create(1L, 1L, 3, new BigDecimal("39.99"), "EUR");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getQuantity());
        assertEquals(new BigDecimal("39.99"), result.getUnitPriceAtPurchase());
        assertEquals("EUR", result.getCurrency());
        verify(purchaseLineRepository).save(any(PurchaseLine.class));
    }

    @Test
    void create_ShouldThrowException_WhenPurchaseIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(null, 1L, 2, new BigDecimal("10"), "EUR")
        );
        assertEquals("Invalid purchase ID", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, null, 2, new BigDecimal("10"), "EUR")
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenQuantityIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, null, new BigDecimal("10"), "EUR")
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenQuantityIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, 0, new BigDecimal("10"), "EUR")
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenQuantityIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, -5, new BigDecimal("10"), "EUR")
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenUnitPriceIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, 2, null, "EUR")
        );
        assertEquals("Unit price must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenUnitPriceIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, 2, new BigDecimal("-10"), "EUR")
        );
        assertEquals("Unit price must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenCurrencyIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, 2, new BigDecimal("10"), null)
        );
        assertEquals("Currency cannot be null or empty", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenCurrencyIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 1L, 2, new BigDecimal("10"), "   ")
        );
        assertEquals("Currency cannot be null or empty", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(99L, 1L, 2, new BigDecimal("10"), "EUR")
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.create(1L, 99L, 2, new BigDecimal("10"), "EUR")
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnPurchaseLine_WhenIdExists() {
        // Arrange
        when(purchaseLineRepository.findById(1L)).thenReturn(Optional.of(purchaseLine));

        // Act
        Optional<PurchaseLine> result = purchaseLineService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getQuantity());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(purchaseLineRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<PurchaseLine> result = purchaseLineService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.findById(null)
        );
        assertEquals("Invalid purchase line ID", exception.getMessage());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.findById(0L)
        );
        assertEquals("Invalid purchase line ID", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllPurchaseLines() {
        // Arrange
        PurchaseLine line2 = new PurchaseLine();
        line2.setId(2L);
        when(purchaseLineRepository.findAll()).thenReturn(Arrays.asList(purchaseLine, line2));

        // Act
        List<PurchaseLine> result = purchaseLineService.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    // ========== FIND BY PURCHASE TESTS ==========

    @Test
    void findByPurchase_ShouldReturnLines_WhenPurchaseExists() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseLineRepository.findByPurchase(purchase)).thenReturn(Arrays.asList(purchaseLine));

        // Act
        List<PurchaseLine> result = purchaseLineService.findByPurchase(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByPurchase_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.findByPurchase(99L)
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    @Test
    void findByPurchase_ShouldThrowException_WhenPurchaseIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.findByPurchase(null)
        );
        assertEquals("Invalid purchase ID", exception.getMessage());
    }

    // ========== FIND BY GAME TESTS ==========

    @Test
    void findByGame_ShouldReturnLines_WhenGameExists() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(purchaseLineRepository.findByItem(game)).thenReturn(Arrays.asList(purchaseLine));

        // Act
        List<PurchaseLine> result = purchaseLineService.findByGame(1L);

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
                () -> purchaseLineService.findByGame(99L)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void findByGame_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.findByGame(null)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    // ========== UPDATE QUANTITY TESTS ==========

    @Test
    void updateQuantity_ShouldUpdateQuantity_WhenValidData() {
        // Arrange
        when(purchaseLineRepository.findById(1L)).thenReturn(Optional.of(purchaseLine));
        when(purchaseLineRepository.save(any(PurchaseLine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PurchaseLine result = purchaseLineService.updateQuantity(1L, 5);

        // Assert
        assertEquals(5, result.getQuantity());
        verify(purchaseLineRepository).save(purchaseLine);
    }

    @Test
    void updateQuantity_ShouldThrowException_WhenLineNotFound() {
        // Arrange
        when(purchaseLineRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.updateQuantity(99L, 5)
        );
        assertEquals("Purchase line not found with id: 99", exception.getMessage());
    }

    @Test
    void updateQuantity_ShouldThrowException_WhenQuantityIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.updateQuantity(1L, 0)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void updateQuantity_ShouldThrowException_WhenQuantityIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.updateQuantity(1L, -3)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void updateQuantity_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.updateQuantity(null, 5)
        );
        assertEquals("Invalid purchase line ID", exception.getMessage());
    }

    // ========== CALCULATE LINE TOTAL TESTS ==========

    @Test
    void calculateLineTotal_ShouldReturnTotal_WhenLineExists() {
        // Arrange
        when(purchaseLineRepository.findById(1L)).thenReturn(Optional.of(purchaseLine));

        // Act
        BigDecimal result = purchaseLineService.calculateLineTotal(1L);

        // Assert
        assertEquals(new BigDecimal("59.98"), result);
    }

    @Test
    void calculateLineTotal_ShouldThrowException_WhenLineNotFound() {
        // Arrange
        when(purchaseLineRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.calculateLineTotal(99L)
        );
        assertEquals("Purchase line not found with id: 99", exception.getMessage());
    }

    @Test
    void calculateLineTotal_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.calculateLineTotal(null)
        );
        assertEquals("Invalid purchase line ID", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeleteLine_WhenIdExists() {
        // Arrange
        when(purchaseLineRepository.existsById(1L)).thenReturn(true);
        doNothing().when(purchaseLineRepository).deleteById(1L);

        // Act
        purchaseLineService.delete(1L);

        // Assert
        verify(purchaseLineRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenLineNotFound() {
        // Arrange
        when(purchaseLineRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.delete(99L)
        );
        assertEquals("Purchase line not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseLineService.delete(null)
        );
        assertEquals("Invalid purchase line ID", exception.getMessage());
    }
}