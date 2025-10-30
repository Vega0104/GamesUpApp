package com.gamesup.service;

import com.gamesup.entity.Game;
import com.gamesup.entity.Purchase;
import com.gamesup.entity.PurchaseLine;
import com.gamesup.entity.User;
import com.gamesup.repository.GameDAO;
import com.gamesup.repository.PurchaseDAO;
import com.gamesup.repository.PurchaseLineDAO;
import com.gamesup.repository.UserDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock private PurchaseLineDAO purchaseLineDAO;
    @Mock private UserDAO userDAO;
    @Mock private PurchaseDAO purchaseDAO;
    @Mock private GameDAO gameDAO;

    @InjectMocks
    private PurchaseServiceImpl service;

    @Test
    void addToBasket_creePurchaseSiAucunEtAjouteUneLigne() {
        long userId = 1L;
        long gameId = 10L;
        int qty = 2;

        User user = new User();
        user.setId(userId);
        Game game = new Game();
        // Supposons que Game possède getPrice(): float
        game.setPrice(19.99f);

        when(userDAO.getReferenceById(userId)).thenReturn(user);
        when(purchaseDAO.findByUserIdAndStatusOrderByCreatedAtDesc(userId, Purchase.OrderStatus.BASKET))
                .thenReturn(List.of()); // aucun panier existant
        // le save renvoie en général l'entité avec un id; on peut renvoyer la même instance
        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        when(gameDAO.getReferenceById(gameId)).thenReturn(game);

        service.addToBasket(userId, gameId, qty);

        // vérifie qu'on a bien créé un Purchase (save appelé)
        verify(purchaseDAO).save(purchaseCaptor.capture());
        Purchase created = purchaseCaptor.getValue();
        assertThat(created.getUser()).isEqualTo(user);
        assertThat(created.getStatus()).isEqualTo(Purchase.OrderStatus.BASKET);
        assertThat(created.getCurrency()).isEqualTo("EUR");

        // vérifie qu'une PurchaseLine a été persistée avec les bonnes valeurs
        ArgumentCaptor<PurchaseLine> lineCaptor = ArgumentCaptor.forClass(PurchaseLine.class);
        verify(purchaseLineDAO).save(lineCaptor.capture());
        PurchaseLine savedLine = lineCaptor.getValue();
        assertThat(savedLine.getPurchase()).isNotNull();
        assertThat(savedLine.getGame()).isEqualTo(game);
        assertThat(savedLine.getQuantity()).isEqualTo(qty);
        assertThat(savedLine.getUnitPriceAtPurchase()).isEqualTo(19.99f);
    }

    @Test
    void pay_metLeStatutAPaidEtSauvegarde() {
        long purchaseId = 5L;
        Purchase p = new Purchase();
        p.setId(purchaseId);
        p.setStatus(Purchase.OrderStatus.BASKET);
        p.setCreatedAt(LocalDateTime.now());

        when(purchaseDAO.getReferenceById(purchaseId)).thenReturn(p);

        service.pay(purchaseId);

        assertThat(p.getStatus()).isEqualTo(Purchase.OrderStatus.PAID);
        verify(purchaseDAO).save(p);
    }

    @Test
    void computeTotalPrice_sommeQuantityXUnitPrice() {
        long purchaseId = 7L;

        Purchase p = new Purchase();
        p.setId(purchaseId);

        PurchaseLine l1 = new PurchaseLine();
        l1.setQuantity(2);
        l1.setUnitPriceAtPurchase(10.0f);

        PurchaseLine l2 = new PurchaseLine();
        l2.setQuantity(3);
        l2.setUnitPriceAtPurchase(5.5f);

        List<PurchaseLine> lines = new ArrayList<>();
        lines.add(l1);
        lines.add(l2);
        p.setPurchaseLines(lines);

        when(purchaseDAO.getReferenceById(purchaseId)).thenReturn(p);

        float total = service.computeTotalPrice(purchaseId);

        // 2*10 + 3*5.5 = 20 + 16.5 = 36.5
        assertThat(total).isEqualTo(36.5f);
    }
}
