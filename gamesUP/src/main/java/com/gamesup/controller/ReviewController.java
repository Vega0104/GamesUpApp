package com.gamesup.controller;

import com.gamesup.entity.Review;
import com.gamesup.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @DeleteMapping(path = "/review/delete")
    public void deleteReview(long id) {
        reviewService.remove(id);
    }

    @GetMapping(path = "/review/list")
    public List<Review> list() {
        return reviewService.list();
    }

    @PostMapping(path = "/review/add")
    public void addReview(@RequestParam long gameID,
                          @RequestParam long userID,
                          @RequestParam int rating,
                          @RequestParam String review) {
        reviewService.addReview(gameID, userID, rating, review);
    }
}
