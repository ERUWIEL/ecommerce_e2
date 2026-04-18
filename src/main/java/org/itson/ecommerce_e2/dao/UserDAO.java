package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.enums.Role;

@ApplicationScoped
public class UserDAO {

    @Inject
    private EntityManager em;

    public void create(User user) {
        em.persist(user);
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    /**
     * Busca por email — se usa en login y registro para verificar duplicados.
     * Retorna null si no existe.
     * @param email
     * @return 
     */
    public User findByEmail(String email) {
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email",
                    User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.name", User.class)
                .getResultList();
    }

    public List<User> findByRole(Role role) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role ORDER BY u.name",
                User.class)
                .setParameter("role", role)
                .getResultList();
    }

    public List<User> findAllActive() {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.active = true ORDER BY u.name",
                User.class)
                .getResultList();
    }

    public User update(User user) {
        return em.merge(user);
    }

    /**
     * Deshabilita la cuenta sin borrarla — para no perder historial de órdenes.
     * @param id
     */
    public void deactivate(Long id) {
        User u = findById(id);
        if (u != null) {
            u.setActive(false);
            em.merge(u);
        }
    }
}
