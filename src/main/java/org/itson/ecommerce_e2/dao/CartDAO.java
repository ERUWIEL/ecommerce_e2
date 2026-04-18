package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.itson.ecommerce_e2.models.Cart;

@ApplicationScoped
public class CartDAO {

    @Inject
    private EntityManager em;

    public void create(Cart cart) {
        em.persist(cart);
    }

    public Cart findById(Long id) {
        return em.find(Cart.class, id);
    }

    /**
     * Obtiene el carrito de un usuario. Cada usuario tiene exactamente uno — se
     * usa en casi todas las operaciones de carrito.
     * @param userId
     * @return 
     */
    public Cart findByUserId(Long userId) {
        try {
            return em.createQuery(
                    "SELECT c FROM Cart c WHERE c.user.id = :userId",
                    Cart.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Cart update(Cart cart) {
        return em.merge(cart);
    }
}
