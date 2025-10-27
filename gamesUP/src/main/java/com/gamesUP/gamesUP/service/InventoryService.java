package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Inventory;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.repository.InventoryRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final GameRepository gameRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            GameRepository gameRepository) {
        this.inventoryRepository = inventoryRepository;
        this.gameRepository = gameRepository;
    }

    public Inventory create(Long gameId, String sku,
                            BigDecimal basePrice, String currency, Integer stockQuantity) {
        // Validations
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price must be greater than or equal to 0");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than or equal to 0");
        }

        String trimmedSku = sku.trim();

        if (inventoryRepository.existsBySku(trimmedSku)) {
            throw new IllegalArgumentException("SKU already exists: " + trimmedSku);
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        Inventory item = new Inventory();
        item.setProduct(game);
        item.setSku(trimmedSku);
        item.setBasePrice(basePrice);
        item.setCurrency(currency);
        item.setStockQuantity(stockQuantity);
        item.setActive(true);

        return inventoryRepository.save(item);
    }

    public Optional<Inventory> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }
        return inventoryRepository.findById(id);
    }

    public Optional<Inventory> findBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        return inventoryRepository.findBySku(sku.trim());
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> findByGame(Long gameId) {
        if (gameId == null || gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        return inventoryRepository.findByProduct(game);
    }

    public List<Inventory> findActiveItems() {
        return inventoryRepository.findByActiveTrue();
    }

    public List<Inventory> findItemsWithStock() {
        return inventoryRepository.findByStockQuantityGreaterThan(0);
    }

    public Inventory updateStock(Long id, Integer newStockQuantity) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }
        if (newStockQuantity == null || newStockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than or equal to 0");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        item.setStockQuantity(newStockQuantity);

        return inventoryRepository.save(item);
    }

    public Inventory incrementStock(Long id, Integer quantity) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        item.setStockQuantity(item.getStockQuantity() + quantity);

        return inventoryRepository.save(item);
    }

    public Inventory decrementStock(Long id, Integer quantity) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        if (item.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + item.getStockQuantity() + ", requested: " + quantity);
        }

        item.setStockQuantity(item.getStockQuantity() - quantity);

        return inventoryRepository.save(item);
    }

    public Inventory updatePrice(Long id, BigDecimal newPrice) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        item.setBasePrice(newPrice);

        return inventoryRepository.save(item);
    }

    public Inventory activate(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        item.setActive(true);

        return inventoryRepository.save(item);
    }

    public Inventory deactivate(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        item.setActive(false);

        return inventoryRepository.save(item);
    }

    public boolean hasStock(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }

        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found with id: " + id));

        return item.getStockQuantity() > 0;
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid inventory ID");
        }

        if (!inventoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Inventory not found with id: " + id);
        }

        inventoryRepository.deleteById(id);
    }
}