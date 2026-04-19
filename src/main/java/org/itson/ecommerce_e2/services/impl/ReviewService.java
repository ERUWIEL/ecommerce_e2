package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.dao.ProductDAO;
import org.itson.ecommerce_e2.dao.ReviewDAO;
import org.itson.ecommerce_e2.dao.UserDAO;
import org.itson.ecommerce_e2.models.Product;
import org.itson.ecommerce_e2.models.Review;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.services.IReviewService;

@ApplicationScoped
public class ReviewService implements IReviewService {

    @Inject
    private EntityManager em;
    @Inject
    private ReviewDAO reviewDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private ProductDAO productDAO;

    @Override
    public Review create(Long userId, Long productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("El rating debe estar entre 1 y 5.");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Producto no encontrado: " + productId);
        }

        if (reviewDAO.existsByUserAndProduct(userId, productId)) {
            throw new IllegalStateException("Ya dejaste una reseña para este producto.");
        }

        em.getTransaction().begin();
        try {
            Review review = new Review(user, product, rating, comment);
            reviewDAO.create(review);
            em.getTransaction().commit();
            return review;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public List<Review> findByProduct(Long productId) {
        return reviewDAO.findByProductId(productId);
    }

    @Override
    public List<Review> findByUser(Long userId) {
        return reviewDAO.findByUserId(userId);
    }

    @Override
    public double getAverageRating(Long productId) {
        return reviewDAO.getAverageRating(productId);
    }

    @Override
    public void delete(Long reviewId) {
        em.getTransaction().begin();
        try {
            reviewDAO.delete(reviewId);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
