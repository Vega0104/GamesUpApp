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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private PurchaseLineRepository purchaseLineRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseService purchaseService;

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
        purchase.setLines(new ArrayList<>());

        purchaseLine = new PurchaseLine();
        purchaseLine.setId(1L);
        purchaseLine.setPurchase(purchase);
        purchaseLine.setItem(game);
        purchaseLine.setQuantity(2);
        purchaseLine.setUnitPriceAtPurchase(new BigDecimal("29.99"));
        purchaseLine.setCurrency("EUR");
    }

    // ========== CREATE PURCHASE TESTS ==========

    @Test
    void createPurchase_ShouldCreatePurchase_WhenValidData() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        // Act
        Purchase result = purchaseService.createPurchase(1L, "EUR");

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getPlacedBy());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals("EUR", result.getCurrency());
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void createPurchase_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(null, "EUR")
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void createPurchase_ShouldThrowException_WhenCurrencyIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(1L, null)
        );
        assertEquals("Currency cannot be null or empty", exception.getMessage());
    }

    @Test
    void createPurchase_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(99L, "EUR")
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    // ========== ADD LINE TESTS ==========

    @Test
    void addLine_ShouldAddLine_WhenValidData() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Purchase result = purchaseService.addLine(1L, 1L, 2);

        // Assert
        assertEquals(1, result.getLines().size());
        assertEquals(new BigDecimal("59.98"), result.getTotalAmount());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void addLine_ShouldThrowException_WhenPurchaseIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(null, 1L, 2)
        );
        assertEquals("Invalid purchase ID", exception.getMessage());
    }

    @Test
    void addLine_ShouldThrowException_WhenGameIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(1L, null, 2)
        );
        assertEquals("Invalid game ID", exception.getMessage());
    }

    @Test
    void addLine_ShouldThrowException_WhenQuantityIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(1L, 1L, 0)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void addLine_ShouldThrowException_WhenQuantityIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(1L, 1L, -5)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void addLine_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(99L, 1L, 2)
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    @Test
    void addLine_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(1L, 99L, 2)
        );
        assertEquals("Game not found with id: 99", exception.getMessage());
    }

    @Test
    void addLine_ShouldThrowException_WhenPurchaseNotPending() {
        // Arrange
        purchase.setStatus(OrderStatus.PAID);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.addLine(1L, 1L, 2)
        );
        assertEquals("Cannot modify purchase with status: PAID", exception.getMessage());
    }

    // ========== REMOVE LINE TESTS ==========

    @Test
    void removeLine_ShouldRemoveLine_WhenLineExists() {
        // Arrange
        purchase.getLines().add(purchaseLine);
        purchase.setTotalAmount(new BigDecimal("59.98"));
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Purchase result = purchaseService.removeLine(1L, 1L);

        // Assert
        assertEquals(0, result.getLines().size());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void removeLine_ShouldThrowException_WhenPurchaseIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.removeLine(null, 1L)
        );
        assertEquals("Invalid purchase ID", exception.getMessage());
    }

    @Test
    void removeLine_ShouldThrowException_WhenLineIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.removeLine(1L, null)
        );
        assertEquals("Invalid line ID", exception.getMessage());
    }

    @Test
    void removeLine_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.removeLine(99L, 1L)
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    @Test
    void removeLine_ShouldThrowException_WhenLineNotFound() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.removeLine(1L, 99L)
        );
        assertEquals("Line not found with id: 99", exception.getMessage());
    }

    @Test
    void removeLine_ShouldThrowException_WhenPurchaseNotPending() {
        // Arrange
        purchase.setStatus(OrderStatus.PAID);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.removeLine(1L, 1L)
        );
        assertEquals("Cannot modify purchase with status: PAID", exception.getMessage());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnPurchase_WhenIdExists() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act
        Optional<Purchase> result = purchaseService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(OrderStatus.PENDING, result.get().getStatus());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Purchase> result = purchaseService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.findById(null)
        );
        assertEquals("Invalid purchase ID", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllPurchases() {
        // Arrange
        Purchase purchase2 = new Purchase();
        purchase2.setId(2L);
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(purchase, purchase2));

        // Act
        List<Purchase> result = purchaseService.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    // ========== FIND BY USER TESTS ==========

    @Test
    void findByUser_ShouldReturnPurchases_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseRepository.findByPlacedByOrderByCreatedAtDesc(user)).thenReturn(Arrays.asList(purchase));

        // Act
        List<Purchase> result = purchaseService.findByUser(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.findByUser(99L)
        );
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void findByUser_ShouldThrowException_WhenUserIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.findByUser(null)
        );
        assertEquals("Invalid user ID", exception.getMessage());
    }

    // ========== FIND BY STATUS TESTS ==========

    @Test
    void findByStatus_ShouldReturnPurchases_WhenStatusMatches() {
        // Arrange
        when(purchaseRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Arrays.asList(purchase));

        // Act
        List<Purchase> result = purchaseService.findByStatus(OrderStatus.PENDING);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void findByStatus_ShouldThrowException_WhenStatusIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.findByStatus(null)
        );
        assertEquals("Status cannot be null", exception.getMessage());
    }

    // ========== MARK AS PAID TESTS ==========

    @Test
    void markAsPaid_ShouldMarkAsPaid_WhenPurchaseIsPending() {
        // Arrange
        purchase.getLines().add(purchaseLine);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Purchase result = purchaseService.markAsPaid(1L);

        // Assert
        assertEquals(OrderStatus.PAID, result.getStatus());
        assertNotNull(result.getPaidAt());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void markAsPaid_ShouldThrowException_WhenPurchaseNotPending() {
        // Arrange
        purchase.setStatus(OrderStatus.PAID);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.markAsPaid(1L)
        );
        assertEquals("Can only mark PENDING purchases as paid", exception.getMessage());
    }

    @Test
    void markAsPaid_ShouldThrowException_WhenPurchaseIsEmpty() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.markAsPaid(1L)
        );
        assertEquals("Cannot mark empty purchase as paid", exception.getMessage());
    }

    @Test
    void markAsPaid_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.markAsPaid(99L)
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    // ========== MARK AS SHIPPED TESTS ==========

    @Test
    void markAsShipped_ShouldMarkAsShipped_WhenPurchaseIsPaid() {
        // Arrange
        purchase.setStatus(OrderStatus.PAID);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Purchase result = purchaseService.markAsShipped(1L);

        // Assert
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void markAsShipped_ShouldThrowException_WhenPurchaseNotPaid() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.markAsShipped(1L)
        );
        assertEquals("Can only ship PAID purchases", exception.getMessage());
    }

    // ========== MARK AS DELIVERED TESTS ==========

    @Test
    void markAsDelivered_ShouldMarkAsDelivered_WhenPurchaseIsShipped() {
        // Arrange
        purchase.setStatus(OrderStatus.SHIPPED);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Purchase result = purchaseService.markAsDelivered(1L);

        // Assert
        assertEquals(OrderStatus.DELIVERED, result.getStatus());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void markAsDelivered_ShouldThrowException_WhenPurchaseNotShipped() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.markAsDelivered(1L)
        );
        assertEquals("Can only deliver SHIPPED purchases", exception.getMessage());
    }

    // ========== CANCEL TESTS ==========

    @Test
    void cancel_ShouldCancelPurchase_WhenNotDelivered() {
        // Arrange
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Purchase result = purchaseService.cancel(1L);

        // Assert
        assertEquals(OrderStatus.CANCELED, result.getStatus());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void cancel_ShouldThrowException_WhenPurchaseDelivered() {
        // Arrange
        purchase.setStatus(OrderStatus.DELIVERED);
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.cancel(1L)
        );
        assertEquals("Cannot cancel DELIVERED purchase", exception.getMessage());
    }

    @Test
    void cancel_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.cancel(99L)
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeletePurchase_WhenIdExists() {
        // Arrange
        when(purchaseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(purchaseRepository).deleteById(1L);

        // Act
        purchaseService.delete(1L);

        // Assert
        verify(purchaseRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenPurchaseNotFound() {
        // Arrange
        when(purchaseRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.delete(99L)
        );
        assertEquals("Purchase not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.delete(null)
        );
        assertEquals("Invalid purchase ID", exception.getMessage());
    }
}