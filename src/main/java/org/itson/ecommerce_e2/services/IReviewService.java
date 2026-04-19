package org.itson.ecommerce_e2.services;

import java.util.List;
import org.itson.ecommerce_e2.models.Review;

public interface IReviewService {

    /**
     * Crea una reseña. Lanza IllegalStateException si el usuario ya reseñó ese
     * producto. Lanza IllegalArgumentException si rating < 1 o rating > 5.
     * @param userId
     * @param productId
     * @param rating
     * @param comment
     * @return 
     */
    Review create(Long userId, Long productId, int rating, String comment);

    List<Review> findByProduct(Long productId);

    List<Review> findByUser(Long userId);

    double getAverageRating(Long productId);

    void delete(Long reviewId);
}
