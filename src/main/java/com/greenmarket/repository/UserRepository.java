package com.greenmarket.repository;

import com.greenmarket.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

@ApplicationScoped
public class UserRepository extends GenericRepository<User, Integer> {

    public UserRepository() {
        super(User.class);
    }

    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.user = :user", User.class)
                    .setParameter("user", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByCredentials(String username, String password) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.user = :user AND u.password = :password", User.class)
                    .setParameter("user", username)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
