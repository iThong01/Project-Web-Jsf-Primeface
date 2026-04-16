package com.greenmarket.service;

import com.greenmarket.model.User;
import com.greenmarket.dao.UserDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    @Inject
    private UserDAO userRepo;

    public User login(String username, String password) {
        return userRepo.findByCredentials(username, password);
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Transactional
    public void register(User user) {
        user.setRole("customer");
        userRepo.save(user);
    }

    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        User user = userRepo.findById(userId);
        if (user != null) {
            user.setPassword(newPassword);
            userRepo.update(user);
        }
    }
}
