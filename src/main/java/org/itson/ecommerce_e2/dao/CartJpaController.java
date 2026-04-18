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
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.CartItem;
import java.util.ArrayList;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.IllegalOrphanException;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.Cart;

/**
 *
 * @author gatog
 */
public class CartJpaController implements Serializable {

    public CartJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cart cart) throws IllegalOrphanException {
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<CartItem>());
        }
        List<String> illegalOrphanMessages = null;
        User userOrphanCheck = cart.getUser();
        if (userOrphanCheck != null) {
            Cart oldCartOfUser = userOrphanCheck.getCart();
            if (oldCartOfUser != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The User " + userOrphanCheck + " already has an item of type Cart whose user column cannot be null. Please make another selection for the user field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User user = cart.getUser();
            if (user != null) {
                user = em.getReference(user.getClass(), user.getId());
                cart.setUser(user);
            }
            List<CartItem> attachedItems = new ArrayList<CartItem>();
            for (CartItem itemsCartItemToAttach : cart.getItems()) {
                itemsCartItemToAttach = em.getReference(itemsCartItemToAttach.getClass(), itemsCartItemToAttach.getId());
                attachedItems.add(itemsCartItemToAttach);
            }
            cart.setItems(attachedItems);
            em.persist(cart);
            if (user != null) {
                user.setCart(cart);
                user = em.merge(user);
            }
            for (CartItem itemsCartItem : cart.getItems()) {
                Cart oldCartOfItemsCartItem = itemsCartItem.getCart();
                itemsCartItem.setCart(cart);
                itemsCartItem = em.merge(itemsCartItem);
                if (oldCartOfItemsCartItem != null) {
                    oldCartOfItemsCartItem.getItems().remove(itemsCartItem);
                    oldCartOfItemsCartItem = em.merge(oldCartOfItemsCartItem);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cart cart) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cart persistentCart = em.find(Cart.class, cart.getId());
            User userOld = persistentCart.getUser();
            User userNew = cart.getUser();
            List<CartItem> itemsOld = persistentCart.getItems();
            List<CartItem> itemsNew = cart.getItems();
            List<String> illegalOrphanMessages = null;
            if (userNew != null && !userNew.equals(userOld)) {
                Cart oldCartOfUser = userNew.getCart();
                if (oldCartOfUser != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The User " + userNew + " already has an item of type Cart whose user column cannot be null. Please make another selection for the user field.");
                }
            }
            for (CartItem itemsOldCartItem : itemsOld) {
                if (!itemsNew.contains(itemsOldCartItem)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain CartItem " + itemsOldCartItem + " since its cart field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userNew != null) {
                userNew = em.getReference(userNew.getClass(), userNew.getId());
                cart.setUser(userNew);
            }
            List<CartItem> attachedItemsNew = new ArrayList<CartItem>();
            for (CartItem itemsNewCartItemToAttach : itemsNew) {
                itemsNewCartItemToAttach = em.getReference(itemsNewCartItemToAttach.getClass(), itemsNewCartItemToAttach.getId());
                attachedItemsNew.add(itemsNewCartItemToAttach);
            }
            itemsNew = attachedItemsNew;
            cart.setItems(itemsNew);
            cart = em.merge(cart);
            if (userOld != null && !userOld.equals(userNew)) {
                userOld.setCart(null);
                userOld = em.merge(userOld);
            }
            if (userNew != null && !userNew.equals(userOld)) {
                userNew.setCart(cart);
                userNew = em.merge(userNew);
            }
            for (CartItem itemsNewCartItem : itemsNew) {
                if (!itemsOld.contains(itemsNewCartItem)) {
                    Cart oldCartOfItemsNewCartItem = itemsNewCartItem.getCart();
                    itemsNewCartItem.setCart(cart);
                    itemsNewCartItem = em.merge(itemsNewCartItem);
                    if (oldCartOfItemsNewCartItem != null && !oldCartOfItemsNewCartItem.equals(cart)) {
                        oldCartOfItemsNewCartItem.getItems().remove(itemsNewCartItem);
                        oldCartOfItemsNewCartItem = em.merge(oldCartOfItemsNewCartItem);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = cart.getId();
                if (findCart(id) == null) {
                    throw new NonexistentEntityException("The cart with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cart cart;
            try {
                cart = em.getReference(Cart.class, id);
                cart.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cart with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<CartItem> itemsOrphanCheck = cart.getItems();
            for (CartItem itemsOrphanCheckCartItem : itemsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Cart (" + cart + ") cannot be destroyed since the CartItem " + itemsOrphanCheckCartItem + " in its items field has a non-nullable cart field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            User user = cart.getUser();
            if (user != null) {
                user.setCart(null);
                user = em.merge(user);
            }
            em.remove(cart);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Cart> findCartEntities() {
        return findCartEntities(true, -1, -1);
    }

    public List<Cart> findCartEntities(int maxResults, int firstResult) {
        return findCartEntities(false, maxResults, firstResult);
    }

    private List<Cart> findCartEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cart.class));
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

    public Cart findCart(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cart.class, id);
        } finally {
            em.close();
        }
    }

    public int getCartCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cart> rt = cq.from(Cart.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
