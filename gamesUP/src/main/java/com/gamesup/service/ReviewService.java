package com.gamesup.service;

import com.gamesup.entity.Review;

import java.util.List;

public interface ReviewService {
    public void addReview(long gameID, long userID, int rating, String review);

    public List<Review> list();

    public void remove(long id);
}
