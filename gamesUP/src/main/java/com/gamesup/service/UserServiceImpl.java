package com.gamesup.service;

import com.gamesup.entity.User;
import com.gamesup.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDAO userDAO;

    @Override
    public User connexion(String email, String password) {
        return this.userDAO.findByEmailAndAndPassword(email,password);
    }

    @Override
    public void inscription(String email, String pwd) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(pwd);
        this.userDAO.save(user);
    }
}
