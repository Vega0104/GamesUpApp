package com.gamesup.service;

import com.gamesup.entity.User;
import com.gamesup.repository.UserDAO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User connexion(String email, String password) {
        return this.userDAO.findByEmailAndAndPassword(email,password);
    }

    @Override
    public void inscription(String email, String pwd) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(pwd)); // ← Garde seulement cette ligne
        user.setRole(User.Role.CUSTOMER);
        this.userDAO.save(user);
    }

    @Override
    public void updateRole(long userID, User.Role newRole) {
        User user = this.userDAO.findById(userID).orElseThrow();
        user.setRole(newRole);
        this.userDAO.save(user);
    }


    @PostConstruct
    public void createDefaultAdmin() {
        if (!userDAO.findByEmail("admin@gamesup.com").isPresent()) {
            User admin = new User();
            admin.setEmail("admin@gamesup.com");
            admin.setPassword(passwordEncoder.encode("123456")); // ← Garde seulement cette ligne
            admin.setRole(User.Role.ADMIN);
            userDAO.save(admin);
            System.out.println("Admin par défaut créé : admin@gamesup.com");
        }
    }


}
