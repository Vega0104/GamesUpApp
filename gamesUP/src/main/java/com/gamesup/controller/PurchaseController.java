package com.gamesup.controller;

import com.gamesup.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PutMapping(path = "/purchase/pay")
    public void toPay(@RequestParam long purchaseID) {
        purchaseService.pay(purchaseID);
    }

    @GetMapping(path = "/purchase/compute")
    public float computePrice(@RequestParam long idPurchase) {
        float price = purchaseService.computeTotalPrice(idPurchase);
        return price;
    }

    @PostMapping(path = "/purchase/add")
    public void addToBasket(@RequestParam long userID,
                            @RequestParam long gameID,
                            @RequestParam int quantity) {
        purchaseService.addToBasket(userID, gameID, quantity);
    }

    @DeleteMapping(path = "/purchase/delete")
    public void deleteBasket(@RequestParam long purchaseLineID) {
        purchaseService.removeFromBasket(purchaseLineID);
    }
}
