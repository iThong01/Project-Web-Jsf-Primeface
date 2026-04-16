package dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import model.User;

@ApplicationScoped
public class UserDAO extends GenericDAO<User, Integer> {

    public UserDAO() {
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
