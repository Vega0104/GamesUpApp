package com.gamesup.service;

import com.gamesup.entity.User;
import com.gamesup.repository.UserDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    private UserDAO userDAO;

    @Test
    public void loginOK() {
        userService.connexion("client1@gmail.com", "123456");
    }

    @Test
    public void signupOK() {
        int countBefore = userDAO.findAll().size();
        // changé l'email a chaque test pour OK
        userService.inscription("clientTestInscription@gmail.com", "123456");
        int countAfter = userDAO.findAll().size();
        assertEquals(countBefore + 1, countAfter);
        System.out.println("Nombre d'utilisateurs: " + countAfter);
    }
}
