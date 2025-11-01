package com.gamesup.security;

import com.gamesup.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Charge l'utilisateur depuis ta base de données
        com.gamesup.entity.User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Retourne un UserDetails pour Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name()) // ADMIN ou CUSTOMER
                .build();
    }
}