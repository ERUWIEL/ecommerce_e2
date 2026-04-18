package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.itson.ecommerce_e2.models.Review;

@ApplicationScoped
public class ReviewDAO {

    @Inject
    private EntityManager em;

    public void create(Review review) {
        em.persist(review);
    }

    public Review findById(Long id) {
        return em.find(Review.class, id);
    }

    public List<Review> findByProductId(Long productId) {
        return em.createQuery(
                "SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC",
                Review.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    public List<Review> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT r FROM Review r WHERE r.user.id = :userId ORDER BY r.createdAt DESC",
                Review.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Verifica si el usuario ya reseñó el producto. Se usa antes de permitir
     * crear una nueva reseña.
     * @param userId
     * @param productId
     * @return 
     */
    public boolean existsByUserAndProduct(Long userId, Long productId) {
        try {
            em.createQuery(
                    "SELECT r FROM Review r "
                    + "WHERE r.user.id = :userId AND r.product.id = :productId",
                    Review.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    /**
     * Promedio de rating de un producto — para mostrar estrellas en el
     * catálogo. Retorna 0.0 si no tiene reseñas aún.
     * @param productId
     * @return 
     */
    public double getAverageRating(Long productId) {
        try {
            Double avg = (Double) em.createQuery(
                    "SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
                    .setParameter("productId", productId)
                    .getSingleResult();
            return avg != null ? avg : 0.0;
        } catch (NoResultException e) {
            return 0.0;
        }
    }

    public Review update(Review review) {
        return em.merge(review);
    }

    public void delete(Long id) {
        Review r = findById(id);
        if (r != null) {
            em.remove(r);
        }
    }
}
