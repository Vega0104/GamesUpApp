package com.gamesup.controller;

import com.gamesup.entity.User;
import com.gamesup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/role")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserRole(@RequestParam long userID, @RequestParam String role) {
        User.Role newRole = User.Role.valueOf(role);
        userService.updateRole(userID, newRole);
    }
}
