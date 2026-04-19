package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.dao.CartDAO;
import org.itson.ecommerce_e2.dao.UserDAO;
import org.itson.ecommerce_e2.models.Cart;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.enums.Role;
import org.itson.ecommerce_e2.services.IUserService;

@ApplicationScoped
public class UserService implements IUserService {

    @Inject
    private EntityManager em;
    @Inject
    private UserDAO userDAO;
    @Inject
    private CartDAO cartDAO;

    @Override
    public User register(String name, String email, String password, Role role) {
        // Validaciones de negocio — antes de abrir transacción
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (userDAO.existsByEmail(email)) {
            throw new IllegalStateException("Ya existe una cuenta con ese email.");
        }

        em.getTransaction().begin();
        try {
            User user = new User(name, PasswordUtil.hash(password), email, role);
            userDAO.create(user);

            // Crear el carrito del usuario en la misma transacción
            Cart cart = new Cart(user);
            cartDAO.create(cart);

            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public User login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            return null;
        }
        if (!user.isActive()) {
            return null;
        }
        if (!PasswordUtil.matches(password, user.getHashedPassword())) {
            return null;
        }

        return user;
    }

    @Override
    public User findById(Long id) {
        return userDAO.findById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userDAO.findAll();
    }

    @Override
    public User updateProfile(User user) {
        em.getTransaction().begin();
        try {
            User updated = userDAO.update(user);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deactivate(Long id) {
        em.getTransaction().begin();
        try {
            userDAO.deactivate(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDAO.existsByEmail(email);
    }
}
