package com.gamesup.service;

import com.gamesup.entity.Review;
import com.gamesup.repository.GameDAO;
import com.gamesup.repository.UserDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;

    @Test
    public void listReview() {
        List<Review> reviews = reviewService.list();
        for (Review review : reviews) {
            System.out.println("Review ID: " + review.getId()
                    + ", Rating: " + review.getRating()
                    + ", Comment: " + review.getComment());
        }
    }

    @Test
    public void removeReviewOK() {
        reviewService.remove(42);
    }

    @Test
    public void addReviewOK() {
        reviewService.addReview(42,3,3,"Un peu répétitif");
    }
}
