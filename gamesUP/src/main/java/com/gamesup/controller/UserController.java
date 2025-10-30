package com.gamesup.controller;

import com.gamesup.entity.User;
import com.gamesup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping(path = "/login")
    public User login(String email, String password) {
        return userService.connexion(email, password);
    }

    @PostMapping(path = "/signup")
    public void signup(String email, String password) {
        userService.inscription(email,password);
    }
}
