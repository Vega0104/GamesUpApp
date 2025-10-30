package com.gamesup.service;

import com.gamesup.entity.Review;
import com.gamesup.repository.GameDAO;
import com.gamesup.repository.ReviewDAO;
import com.gamesup.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewDAO reviewDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private GameDAO gameDAO;


    @Override
    public void addReview(long gameID, long userID, int rating, String comment) {

        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        review.setUser(this.userDAO.getReferenceById(userID));
        review.setGame(this.gameDAO.getReferenceById(gameID));

        this.reviewDAO.save(review);
    }

    @Override
    public List<Review> list() {
        return this.reviewDAO.findAll();
    }

    @Override
    public void remove(long id) {
        this.reviewDAO.deleteById(id);

    }
}
