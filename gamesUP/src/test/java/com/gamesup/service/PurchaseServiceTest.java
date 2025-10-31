package com.gamesup.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PurchaseServiceTest {
    @Autowired
    PurchaseService purchaseService;

    @Test
    public void addToBasketOK() {
        purchaseService.addToBasket(3,91, 1);
    };

    @Test
    public void removeFromBasketOK() {
        purchaseService.removeFromBasket(2);
    }

    @Test
    public void computePriceOK() {
        System.out.println(purchaseService.computeTotalPrice(2));
    }

    @Test
    public void payPurchaseOK() {
        purchaseService.pay(2);
    }

}
