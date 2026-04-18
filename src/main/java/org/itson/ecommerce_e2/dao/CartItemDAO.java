package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.itson.ecommerce_e2.models.CartItem;

@ApplicationScoped
public class CartItemDAO {

    @Inject
    private EntityManager em;

    public void create(CartItem item) {
        em.persist(item);
    }

    public CartItem findById(Long id) {
        return em.find(CartItem.class, id);
    }

    public List<CartItem> findByCartId(Long cartId) {
        return em.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId",
                CartItem.class)
                .setParameter("cartId", cartId)
                .getResultList();
    }

    /**
     * Busca si un producto ya existe en el carrito. Se usa para incrementar
     * cantidad en lugar de crear un duplicado.
     * @param cartId
     * @param productId
     * @return 
     */
    public CartItem findByCartAndProduct(Long cartId, Long productId) {
        try {
            return em.createQuery(
                    "SELECT ci FROM CartItem ci "
                    + "WHERE ci.cart.id = :cartId AND ci.product.id = :productId",
                    CartItem.class)
                    .setParameter("cartId", cartId)
                    .setParameter("productId", productId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public CartItem update(CartItem item) {
        return em.merge(item);
    }

    public void delete(CartItem item) {
        em.remove(em.contains(item) ? item : em.merge(item));
    }

    /**
     * Vacía el carrito completo — se llama después de hacer checkout.
     * @param cartId
     */
    public void deleteAllByCartId(Long cartId) {
        em.createQuery("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
                .setParameter("cartId", cartId)
                .executeUpdate();
    }
}
