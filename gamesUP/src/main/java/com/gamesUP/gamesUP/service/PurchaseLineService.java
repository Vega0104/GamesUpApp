package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.PurchaseLine;
import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.repository.PurchaseLineRepository;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PurchaseLineService {

    private final PurchaseLineRepository purchaseLineRepository;
    private final PurchaseRepository purchaseRepository;
    private final GameRepository gameRepository;

    public PurchaseLineService(PurchaseLineRepository purchaseLineRepository,
                               PurchaseRepository purchaseRepository,
                               GameRepository gameRepository) {
        this.purchaseLineRepository = purchaseLineRepository;
        this.purchaseRepository = purchaseRepository;
        this.gameRepository = gameRepository;
    }

    public PurchaseLine create(Long purchaseId, Long gameId, Integer quantity, BigDecimal unitPrice, String currency) {
        // Validations
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be greater than or equal to 0");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        PurchaseLine line = new PurchaseLine();
        line.setPurchase(purchase);
        line.setItem(game);
        line.setQuantity(quantity);
        line.setUnitPriceAtPurchase(unitPrice);
        line.setCurrency(currency.trim());

        return purchaseLineRepository.save(line);
    }

    public Optional<PurchaseLine> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid purchase line ID");
        }
        return purchaseLineRepository.findById(id);
    }

    public List<PurchaseLine> findAll() {
        return purchaseLineRepository.findAll();
    }

    public List<PurchaseLine> findByPurchase(Long purchaseId) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Invalid purchase ID");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + purchaseId));

        return purchaseLineRepository.findByPurchase(purchase);
    }

    public List<PurchaseLine> findByGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        return purchaseLineRepository.findByItem(game);
    }

    public PurchaseLine updateQuantity(Long id, Integer newQuantity) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid purchase line ID");
        }
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        PurchaseLine line = purchaseLineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase line not found with id: " + id));

        line.setQuantity(newQuantity);

        return purchaseLineRepository.save(line);
    }

    public BigDecimal calculateLineTotal(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid purchase line ID");
        }

        PurchaseLine line = purchaseLineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase line not found with id: " + id));

        return line.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(line.getQuantity()));
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid purchase line ID");
        }

        if (!purchaseLineRepository.existsById(id)) {
            throw new IllegalArgumentException("Purchase line not found with id: " + id);
        }

        purchaseLineRepository.deleteById(id);
    }
}