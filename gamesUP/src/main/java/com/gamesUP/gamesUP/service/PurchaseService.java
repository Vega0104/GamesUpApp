package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.PurchaseLine;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.OrderStatus;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.PurchaseLineRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseLineRepository purchaseLineRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           PurchaseLineRepository purchaseLineRepository,
                           UserRepository userRepository,
                           GameRepository gameRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseLineRepository = purchaseLineRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    public Purchase createPurchase(Long userId, String currency) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Purchase purchase = new Purchase();
        purchase.setPlacedBy(user);
        purchase.setStatus(OrderStatus.PENDING);
        purchase.setTotalAmount(BigDecimal.ZERO);
        purchase.setCurrency(currency.trim());
        purchase.setLines(new ArrayList<>());

        return purchaseRepository.save(purchase);
    }

    public Purchase addLine(Long purchaseId, Long gameId, Integer quantity) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify purchase with status: " + purchase.getStatus());
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        // Créer la ligne
        PurchaseLine line = new PurchaseLine();
        line.setPurchase(purchase);
        line.setItem(game);
        line.setQuantity(quantity);
        line.setUnitPriceAtPurchase(game.getBasePrice());
        line.setCurrency(game.getCurrency());

        purchase.getLines().add(line);

        // Recalculer le total
        recalculateTotal(purchase);

        return purchaseRepository.save(purchase);
    }

    public Purchase removeLine(Long purchaseId, Long lineId) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }
        if (lineId == null || lineId <= 0) {
            throw new IllegalArgumentException("Invalid line ID");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify purchase with status: " + purchase.getStatus());
        }

        PurchaseLine lineToRemove = purchase.getLines().stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Line not found with id: " + lineId));

        purchase.getLines().remove(lineToRemove);

        // Recalculer le total
        recalculateTotal(purchase);

        return purchaseRepository.save(purchase);
    }

    public Optional<Purchase> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }
        return purchaseRepository.findById(id);
    }

    public List<Purchase> findAll() {
        return purchaseRepository.findAll();
    }

    public List<Purchase> findByUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return purchaseRepository.findByPlacedByOrderByCreatedAtDesc(user);
    }

    public List<Purchase> findByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return purchaseRepository.findByStatus(status);
    }

    public Purchase markAsPaid(Long purchaseId) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Can only mark PENDING purchases as paid");
        }

        if (purchase.getLines().isEmpty()) {
            throw new IllegalArgumentException("Cannot mark empty purchase as paid");
        }

        purchase.setStatus(OrderStatus.PAID);
        purchase.setPaidAt(LocalDateTime.now());

        return purchaseRepository.save(purchase);
    }

    public Purchase markAsShipped(Long purchaseId) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Can only ship PAID purchases");
        }

        purchase.setStatus(OrderStatus.SHIPPED);

        return purchaseRepository.save(purchase);
    }

    public Purchase markAsDelivered(Long purchaseId) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Can only deliver SHIPPED purchases");
        }

        purchase.setStatus(OrderStatus.DELIVERED);

        return purchaseRepository.save(purchase);
    }

    public Purchase cancel(Long purchaseId) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        if (purchase.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel DELIVERED purchase");
        }

        purchase.setStatus(OrderStatus.CANCELED);

        return purchaseRepository.save(purchase);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }

        if (!purchaseRepository.existsById(id)) {
            throw new IllegalArgumentException("Purchase not found with id: " + id);
        }

        purchaseRepository.deleteById(id);
    }

    private void recalculateTotal(Purchase purchase) {
        BigDecimal total = purchase.getLines().stream()
                .map(line -> line.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        purchase.setTotalAmount(total);
    }
}