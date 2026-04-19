package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.itson.ecommerce_e2.dao.CartDAO;
import org.itson.ecommerce_e2.dao.CartItemDAO;
import org.itson.ecommerce_e2.dao.ProductDAO;
import org.itson.ecommerce_e2.models.Cart;
import org.itson.ecommerce_e2.models.CartItem;
import org.itson.ecommerce_e2.models.Product;
import org.itson.ecommerce_e2.services.ICartService;

@ApplicationScoped
public class CartService implements ICartService {

    @Inject
    private EntityManager em;
    @Inject
    private CartDAO cartDAO;
    @Inject
    private CartItemDAO cartItemDAO;
    @Inject
    private ProductDAO productDAO;

    @Override
    public Cart addItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }

        Cart cart = cartDAO.findByUserId(userId);
        if (cart == null) {
            throw new IllegalStateException("El usuario no tiene carrito. Id: " + userId);
        }

        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Producto no encontrado: " + productId);
        }
        if (!product.isActive()) {
            throw new IllegalStateException("El producto ya no está disponible.");
        }

        em.getTransaction().begin();
        try {
            // Si el producto ya está en el carrito → solo incrementar cantidad
            CartItem existing = cartItemDAO.findByCartAndProduct(cart.getId(), productId);
            if (existing != null) {
                int newQty = existing.getQuantity() + quantity;
                if (newQty > product.getStock()) {
                    throw new IllegalStateException(
                            "Stock insuficiente. Disponible: " + product.getStock());
                }
                existing.setQuantity(newQty);
                cartItemDAO.update(existing);
            } else {
                if (quantity > product.getStock()) {
                    throw new IllegalStateException(
                            "Stock insuficiente. Disponible: " + product.getStock());
                }
                CartItem item = new CartItem(cart, product, quantity);
                cartItemDAO.create(item);
            }

            em.getTransaction().commit();
            return cartDAO.findByUserId(userId);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Cart updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemDAO.findById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Ítem de carrito no encontrado: " + cartItemId);
        }

        Long userId = item.getCart().getUser().getId();

        em.getTransaction().begin();
        try {
            if (quantity <= 0) {
                cartItemDAO.delete(item);
            } else {
                if (quantity > item.getProduct().getStock()) {
                    throw new IllegalStateException(
                            "Stock insuficiente. Disponible: " + item.getProduct().getStock());
                }
                item.setQuantity(quantity);
                cartItemDAO.update(item);
            }
            em.getTransaction().commit();
            return cartDAO.findByUserId(userId);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Cart removeItem(Long cartItemId) {
        CartItem item = cartItemDAO.findById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Ítem no encontrado: " + cartItemId);
        }

        Long userId = item.getCart().getUser().getId();

        em.getTransaction().begin();
        try {
            cartItemDAO.delete(item);
            em.getTransaction().commit();
            return cartDAO.findByUserId(userId);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        Cart cart = cartDAO.findByUserId(userId);
        if (cart == null) {
            throw new IllegalStateException("Carrito no encontrado para usuario: " + userId);
        }
        return cart;
    }

    @Override
    public void clear(Long userId) {
        Cart cart = cartDAO.findByUserId(userId);
        if (cart == null) {
            return;
        }

        em.getTransaction().begin();
        try {
            cartItemDAO.deleteAllByCartId(cart.getId());
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
