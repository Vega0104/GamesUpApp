package com.gamesup.service;

public interface PurchaseService {
    public void addToBasket(long userID, long gameID, int quantity);
    public void removeFromBasket(long purchaseLineID);
    public void pay(long purchaseID);
    public float computeTotalPrice(long purchaseID);
}
