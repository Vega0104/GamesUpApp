package com.gamesup.service;

import com.gamesup.entity.User;

public interface UserService {
    public User connexion(String email, String password);
    public void inscription(String email, String password);
    public void updateRole(long userID, User.Role newRole);
}

