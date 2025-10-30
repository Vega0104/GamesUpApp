package com.gamesup.service;

import com.gamesup.entity.Game;
import com.gamesup.entity.Purchase;
import com.gamesup.entity.PurchaseLine;
import com.gamesup.entity.User;
import com.gamesup.repository.GameDAO;
import com.gamesup.repository.PurchaseDAO;
import com.gamesup.repository.PurchaseLineDAO;
import com.gamesup.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseServiceImpl implements PurchaseService{

    @Autowired
    private PurchaseLineDAO purchaseLineDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PurchaseDAO purchaseDAO;

    @Autowired
    private GameDAO gameDAO;

    /**
     * Crée nouveau Purchase en BD si existe pas encore ( on a déjà un f° qui récup les derniers basket d'un user ds stats
     * OU récupère le le plus récent existant
     * Y ajoute un nouveau PurchaseLine
     * @param userID
     * @param gameID
     * @param quantity
     */
    @Override
    public void addToBasket(long userID, long gameID, int quantity) {

        User user = userDAO.getReferenceById(userID);
        List<Purchase> purchases = purchaseDAO.findByUserIdAndStatusOrderByCreatedAtDesc(userID,Purchase.OrderStatus.BASKET);
        Purchase purchase;
        if (purchases.size() > 0) {
            purchase = purchases.getFirst();
        } else {
            purchase = new Purchase();
            purchase.setUser(user);
            purchase.setStatus(Purchase.OrderStatus.BASKET);
            purchase.setCurrency("EUR");
            purchase.setCreatedAt(LocalDateTime.now());
            this.purchaseDAO.save(purchase);
        }

        // Purchaise exite

        Game game = gameDAO.getReferenceById(gameID);
        PurchaseLine purchaseLine = new PurchaseLine();
        purchaseLine.setPurchase(purchase);
        purchaseLine.setGame(game);
        purchaseLine.setQuantity(quantity);
        purchaseLine.setUnitPriceAtPurchase(game.getPrice());

        this.purchaseLineDAO.save(purchaseLine);


    }

    @Override
    public void removeFromBasket(long purchaseLineID) {
        this.purchaseLineDAO.deleteById(purchaseLineID);
    }

    @Override
    public void pay(long purchaseID) {
        // etape 1 : recuperer le purchase par son ID
        Purchase purchase = this.purchaseDAO.getReferenceById(purchaseID);

        // etape 2 : modifier le statut du purchase
        purchase.setStatus(Purchase.OrderStatus.PAID);

        // etape 3 : save l'entity purchase
        this.purchaseDAO.save(purchase);
    }

    @Override
    public float computeTotalPrice(long purchaseID) {
        // recuperer purchase par ID
        Purchase purchase = this.purchaseDAO.getReferenceById(purchaseID);

        // calcul prix total en add quantiy x price des purchase line
        float totalPrice = 0;
        for( PurchaseLine purchaseLine : purchase.getPurchaseLines() ) {
            float price = purchaseLine.getQuantity() * purchaseLine.getUnitPriceAtPurchase();
            totalPrice = totalPrice + price;
        }

        return totalPrice;


    }
}
