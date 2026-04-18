/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.itson.ecommerce_e2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.Cart;
import org.itson.ecommerce_e2.models.CartItem;
import org.itson.ecommerce_e2.models.Product;

/**
 *
 * @author gatog
 */
public class CartItemJpaController implements Serializable {

    public CartItemJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CartItem cartItem) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cart cart = cartItem.getCart();
            if (cart != null) {
                cart = em.getReference(cart.getClass(), cart.getId());
                cartItem.setCart(cart);
            }
            Product product = cartItem.getProduct();
            if (product != null) {
                product = em.getReference(product.getClass(), product.getId());
                cartItem.setProduct(product);
            }
            em.persist(cartItem);
            if (cart != null) {
                cart.getItems().add(cartItem);
                cart = em.merge(cart);
            }
            if (product != null) {
                product.getCartItems().add(cartItem);
                product = em.merge(product);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CartItem cartItem) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CartItem persistentCartItem = em.find(CartItem.class, cartItem.getId());
            Cart cartOld = persistentCartItem.getCart();
            Cart cartNew = cartItem.getCart();
            Product productOld = persistentCartItem.getProduct();
            Product productNew = cartItem.getProduct();
            if (cartNew != null) {
                cartNew = em.getReference(cartNew.getClass(), cartNew.getId());
                cartItem.setCart(cartNew);
            }
            if (productNew != null) {
                productNew = em.getReference(productNew.getClass(), productNew.getId());
                cartItem.setProduct(productNew);
            }
            cartItem = em.merge(cartItem);
            if (cartOld != null && !cartOld.equals(cartNew)) {
                cartOld.getItems().remove(cartItem);
                cartOld = em.merge(cartOld);
            }
            if (cartNew != null && !cartNew.equals(cartOld)) {
                cartNew.getItems().add(cartItem);
                cartNew = em.merge(cartNew);
            }
            if (productOld != null && !productOld.equals(productNew)) {
                productOld.getCartItems().remove(cartItem);
                productOld = em.merge(productOld);
            }
            if (productNew != null && !productNew.equals(productOld)) {
                productNew.getCartItems().add(cartItem);
                productNew = em.merge(productNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = cartItem.getId();
                if (findCartItem(id) == null) {
                    throw new NonexistentEntityException("The cartItem with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CartItem cartItem;
            try {
                cartItem = em.getReference(CartItem.class, id);
                cartItem.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cartItem with id " + id + " no longer exists.", enfe);
            }
            Cart cart = cartItem.getCart();
            if (cart != null) {
                cart.getItems().remove(cartItem);
                cart = em.merge(cart);
            }
            Product product = cartItem.getProduct();
            if (product != null) {
                product.getCartItems().remove(cartItem);
                product = em.merge(product);
            }
            em.remove(cartItem);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CartItem> findCartItemEntities() {
        return findCartItemEntities(true, -1, -1);
    }

    public List<CartItem> findCartItemEntities(int maxResults, int firstResult) {
        return findCartItemEntities(false, maxResults, firstResult);
    }

    private List<CartItem> findCartItemEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CartItem.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public CartItem findCartItem(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CartItem.class, id);
        } finally {
            em.close();
        }
    }

    public int getCartItemCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CartItem> rt = cq.from(CartItem.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
