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
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.Product;
import org.itson.ecommerce_e2.models.Review;

/**
 *
 * @author gatog
 */
public class ReviewJpaController implements Serializable {

    public ReviewJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Review review) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User user = review.getUser();
            if (user != null) {
                user = em.getReference(user.getClass(), user.getId());
                review.setUser(user);
            }
            Product product = review.getProduct();
            if (product != null) {
                product = em.getReference(product.getClass(), product.getId());
                review.setProduct(product);
            }
            em.persist(review);
            if (user != null) {
                user.getReviews().add(review);
                user = em.merge(user);
            }
            if (product != null) {
                product.getReviews().add(review);
                product = em.merge(product);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Review review) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Review persistentReview = em.find(Review.class, review.getId());
            User userOld = persistentReview.getUser();
            User userNew = review.getUser();
            Product productOld = persistentReview.getProduct();
            Product productNew = review.getProduct();
            if (userNew != null) {
                userNew = em.getReference(userNew.getClass(), userNew.getId());
                review.setUser(userNew);
            }
            if (productNew != null) {
                productNew = em.getReference(productNew.getClass(), productNew.getId());
                review.setProduct(productNew);
            }
            review = em.merge(review);
            if (userOld != null && !userOld.equals(userNew)) {
                userOld.getReviews().remove(review);
                userOld = em.merge(userOld);
            }
            if (userNew != null && !userNew.equals(userOld)) {
                userNew.getReviews().add(review);
                userNew = em.merge(userNew);
            }
            if (productOld != null && !productOld.equals(productNew)) {
                productOld.getReviews().remove(review);
                productOld = em.merge(productOld);
            }
            if (productNew != null && !productNew.equals(productOld)) {
                productNew.getReviews().add(review);
                productNew = em.merge(productNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = review.getId();
                if (findReview(id) == null) {
                    throw new NonexistentEntityException("The review with id " + id + " no longer exists.");
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
            Review review;
            try {
                review = em.getReference(Review.class, id);
                review.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The review with id " + id + " no longer exists.", enfe);
            }
            User user = review.getUser();
            if (user != null) {
                user.getReviews().remove(review);
                user = em.merge(user);
            }
            Product product = review.getProduct();
            if (product != null) {
                product.getReviews().remove(review);
                product = em.merge(product);
            }
            em.remove(review);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Review> findReviewEntities() {
        return findReviewEntities(true, -1, -1);
    }

    public List<Review> findReviewEntities(int maxResults, int firstResult) {
        return findReviewEntities(false, maxResults, firstResult);
    }

    private List<Review> findReviewEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Review.class));
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

    public Review findReview(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Review.class, id);
        } finally {
            em.close();
        }
    }

    public int getReviewCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Review> rt = cq.from(Review.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
