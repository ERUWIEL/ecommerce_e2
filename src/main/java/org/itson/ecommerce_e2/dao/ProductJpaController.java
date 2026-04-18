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
import org.itson.ecommerce_e2.models.Category;
import org.itson.ecommerce_e2.models.Review;
import java.util.ArrayList;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.IllegalOrphanException;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.CartItem;
import org.itson.ecommerce_e2.models.OrderItem;
import org.itson.ecommerce_e2.models.Product;

/**
 *
 * @author gatog
 */
public class ProductJpaController implements Serializable {

    public ProductJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Product product) {
        if (product.getReviews() == null) {
            product.setReviews(new ArrayList<Review>());
        }
        if (product.getCartItems() == null) {
            product.setCartItems(new ArrayList<CartItem>());
        }
        if (product.getOrderItems() == null) {
            product.setOrderItems(new ArrayList<OrderItem>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Category category = product.getCategory();
            if (category != null) {
                category = em.getReference(category.getClass(), category.getId());
                product.setCategory(category);
            }
            List<Review> attachedReviews = new ArrayList<Review>();
            for (Review reviewsReviewToAttach : product.getReviews()) {
                reviewsReviewToAttach = em.getReference(reviewsReviewToAttach.getClass(), reviewsReviewToAttach.getId());
                attachedReviews.add(reviewsReviewToAttach);
            }
            product.setReviews(attachedReviews);
            List<CartItem> attachedCartItems = new ArrayList<CartItem>();
            for (CartItem cartItemsCartItemToAttach : product.getCartItems()) {
                cartItemsCartItemToAttach = em.getReference(cartItemsCartItemToAttach.getClass(), cartItemsCartItemToAttach.getId());
                attachedCartItems.add(cartItemsCartItemToAttach);
            }
            product.setCartItems(attachedCartItems);
            List<OrderItem> attachedOrderItems = new ArrayList<OrderItem>();
            for (OrderItem orderItemsOrderItemToAttach : product.getOrderItems()) {
                orderItemsOrderItemToAttach = em.getReference(orderItemsOrderItemToAttach.getClass(), orderItemsOrderItemToAttach.getId());
                attachedOrderItems.add(orderItemsOrderItemToAttach);
            }
            product.setOrderItems(attachedOrderItems);
            em.persist(product);
            if (category != null) {
                category.getProducts().add(product);
                category = em.merge(category);
            }
            for (Review reviewsReview : product.getReviews()) {
                Product oldProductOfReviewsReview = reviewsReview.getProduct();
                reviewsReview.setProduct(product);
                reviewsReview = em.merge(reviewsReview);
                if (oldProductOfReviewsReview != null) {
                    oldProductOfReviewsReview.getReviews().remove(reviewsReview);
                    oldProductOfReviewsReview = em.merge(oldProductOfReviewsReview);
                }
            }
            for (CartItem cartItemsCartItem : product.getCartItems()) {
                Product oldProductOfCartItemsCartItem = cartItemsCartItem.getProduct();
                cartItemsCartItem.setProduct(product);
                cartItemsCartItem = em.merge(cartItemsCartItem);
                if (oldProductOfCartItemsCartItem != null) {
                    oldProductOfCartItemsCartItem.getCartItems().remove(cartItemsCartItem);
                    oldProductOfCartItemsCartItem = em.merge(oldProductOfCartItemsCartItem);
                }
            }
            for (OrderItem orderItemsOrderItem : product.getOrderItems()) {
                Product oldProductOfOrderItemsOrderItem = orderItemsOrderItem.getProduct();
                orderItemsOrderItem.setProduct(product);
                orderItemsOrderItem = em.merge(orderItemsOrderItem);
                if (oldProductOfOrderItemsOrderItem != null) {
                    oldProductOfOrderItemsOrderItem.getOrderItems().remove(orderItemsOrderItem);
                    oldProductOfOrderItemsOrderItem = em.merge(oldProductOfOrderItemsOrderItem);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Product product) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Product persistentProduct = em.find(Product.class, product.getId());
            Category categoryOld = persistentProduct.getCategory();
            Category categoryNew = product.getCategory();
            List<Review> reviewsOld = persistentProduct.getReviews();
            List<Review> reviewsNew = product.getReviews();
            List<CartItem> cartItemsOld = persistentProduct.getCartItems();
            List<CartItem> cartItemsNew = product.getCartItems();
            List<OrderItem> orderItemsOld = persistentProduct.getOrderItems();
            List<OrderItem> orderItemsNew = product.getOrderItems();
            List<String> illegalOrphanMessages = null;
            for (Review reviewsOldReview : reviewsOld) {
                if (!reviewsNew.contains(reviewsOldReview)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Review " + reviewsOldReview + " since its product field is not nullable.");
                }
            }
            for (CartItem cartItemsOldCartItem : cartItemsOld) {
                if (!cartItemsNew.contains(cartItemsOldCartItem)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain CartItem " + cartItemsOldCartItem + " since its product field is not nullable.");
                }
            }
            for (OrderItem orderItemsOldOrderItem : orderItemsOld) {
                if (!orderItemsNew.contains(orderItemsOldOrderItem)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain OrderItem " + orderItemsOldOrderItem + " since its product field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (categoryNew != null) {
                categoryNew = em.getReference(categoryNew.getClass(), categoryNew.getId());
                product.setCategory(categoryNew);
            }
            List<Review> attachedReviewsNew = new ArrayList<Review>();
            for (Review reviewsNewReviewToAttach : reviewsNew) {
                reviewsNewReviewToAttach = em.getReference(reviewsNewReviewToAttach.getClass(), reviewsNewReviewToAttach.getId());
                attachedReviewsNew.add(reviewsNewReviewToAttach);
            }
            reviewsNew = attachedReviewsNew;
            product.setReviews(reviewsNew);
            List<CartItem> attachedCartItemsNew = new ArrayList<CartItem>();
            for (CartItem cartItemsNewCartItemToAttach : cartItemsNew) {
                cartItemsNewCartItemToAttach = em.getReference(cartItemsNewCartItemToAttach.getClass(), cartItemsNewCartItemToAttach.getId());
                attachedCartItemsNew.add(cartItemsNewCartItemToAttach);
            }
            cartItemsNew = attachedCartItemsNew;
            product.setCartItems(cartItemsNew);
            List<OrderItem> attachedOrderItemsNew = new ArrayList<OrderItem>();
            for (OrderItem orderItemsNewOrderItemToAttach : orderItemsNew) {
                orderItemsNewOrderItemToAttach = em.getReference(orderItemsNewOrderItemToAttach.getClass(), orderItemsNewOrderItemToAttach.getId());
                attachedOrderItemsNew.add(orderItemsNewOrderItemToAttach);
            }
            orderItemsNew = attachedOrderItemsNew;
            product.setOrderItems(orderItemsNew);
            product = em.merge(product);
            if (categoryOld != null && !categoryOld.equals(categoryNew)) {
                categoryOld.getProducts().remove(product);
                categoryOld = em.merge(categoryOld);
            }
            if (categoryNew != null && !categoryNew.equals(categoryOld)) {
                categoryNew.getProducts().add(product);
                categoryNew = em.merge(categoryNew);
            }
            for (Review reviewsNewReview : reviewsNew) {
                if (!reviewsOld.contains(reviewsNewReview)) {
                    Product oldProductOfReviewsNewReview = reviewsNewReview.getProduct();
                    reviewsNewReview.setProduct(product);
                    reviewsNewReview = em.merge(reviewsNewReview);
                    if (oldProductOfReviewsNewReview != null && !oldProductOfReviewsNewReview.equals(product)) {
                        oldProductOfReviewsNewReview.getReviews().remove(reviewsNewReview);
                        oldProductOfReviewsNewReview = em.merge(oldProductOfReviewsNewReview);
                    }
                }
            }
            for (CartItem cartItemsNewCartItem : cartItemsNew) {
                if (!cartItemsOld.contains(cartItemsNewCartItem)) {
                    Product oldProductOfCartItemsNewCartItem = cartItemsNewCartItem.getProduct();
                    cartItemsNewCartItem.setProduct(product);
                    cartItemsNewCartItem = em.merge(cartItemsNewCartItem);
                    if (oldProductOfCartItemsNewCartItem != null && !oldProductOfCartItemsNewCartItem.equals(product)) {
                        oldProductOfCartItemsNewCartItem.getCartItems().remove(cartItemsNewCartItem);
                        oldProductOfCartItemsNewCartItem = em.merge(oldProductOfCartItemsNewCartItem);
                    }
                }
            }
            for (OrderItem orderItemsNewOrderItem : orderItemsNew) {
                if (!orderItemsOld.contains(orderItemsNewOrderItem)) {
                    Product oldProductOfOrderItemsNewOrderItem = orderItemsNewOrderItem.getProduct();
                    orderItemsNewOrderItem.setProduct(product);
                    orderItemsNewOrderItem = em.merge(orderItemsNewOrderItem);
                    if (oldProductOfOrderItemsNewOrderItem != null && !oldProductOfOrderItemsNewOrderItem.equals(product)) {
                        oldProductOfOrderItemsNewOrderItem.getOrderItems().remove(orderItemsNewOrderItem);
                        oldProductOfOrderItemsNewOrderItem = em.merge(oldProductOfOrderItemsNewOrderItem);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = product.getId();
                if (findProduct(id) == null) {
                    throw new NonexistentEntityException("The product with id " + id + " no longer exists.");
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
            Product product;
            try {
                product = em.getReference(Product.class, id);
                product.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The product with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Review> reviewsOrphanCheck = product.getReviews();
            for (Review reviewsOrphanCheckReview : reviewsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the Review " + reviewsOrphanCheckReview + " in its reviews field has a non-nullable product field.");
            }
            List<CartItem> cartItemsOrphanCheck = product.getCartItems();
            for (CartItem cartItemsOrphanCheckCartItem : cartItemsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the CartItem " + cartItemsOrphanCheckCartItem + " in its cartItems field has a non-nullable product field.");
            }
            List<OrderItem> orderItemsOrphanCheck = product.getOrderItems();
            for (OrderItem orderItemsOrphanCheckOrderItem : orderItemsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the OrderItem " + orderItemsOrphanCheckOrderItem + " in its orderItems field has a non-nullable product field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Category category = product.getCategory();
            if (category != null) {
                category.getProducts().remove(product);
                category = em.merge(category);
            }
            em.remove(product);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Product> findProductEntities() {
        return findProductEntities(true, -1, -1);
    }

    public List<Product> findProductEntities(int maxResults, int firstResult) {
        return findProductEntities(false, maxResults, firstResult);
    }

    private List<Product> findProductEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Product.class));
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

    public Product findProduct(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Product> rt = cq.from(Product.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
