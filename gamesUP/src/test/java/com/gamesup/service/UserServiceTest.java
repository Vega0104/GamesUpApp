package com.gamesup.service;

import com.gamesup.repository.UserDAO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserDAO userDAO;

    @Test
    void loginOK() {
        String email = "client1@example.com";
        String raw = "123456";
        userService.inscription(email, raw);
        var user = userService.connexion(email, raw);
        assertNotNull(user);
        assertEquals(email, user.getEmail());
    }

    @Test
    void signupOK() {
        int before = (int) userDAO.count();
        String email = "admin+" + java.util.UUID.randomUUID() + "@example.com";
        userService.inscription(email, "123456");
        int after = (int) userDAO.count();
        assertEquals(before + 1, after);
    }
}
