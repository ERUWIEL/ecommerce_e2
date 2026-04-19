package org.itson.ecommerce_e2.services;

import java.math.BigDecimal;
import java.util.List;
import org.itson.ecommerce_e2.models.Product;

public interface IProductService {

    Product create(String name, String description, BigDecimal price,
            int stock, Long categoryId, String imageKey);

    Product findById(Long id);

    List<Product> findAllActive();

    List<Product> findByCategory(Long categoryId);

    List<Product> search(String keyword);

    Product update(Product product);

    void deactivate(Long id);

    /**
     * Reduce el stock del producto al confirmar una compra. Lanza
     * IllegalStateException si no hay stock suficiente.
     * @param productId
     * @param quantity
     */
    void reduceStock(Long productId, int quantity);
}
