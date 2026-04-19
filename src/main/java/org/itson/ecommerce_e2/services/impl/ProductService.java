package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.itson.ecommerce_e2.dao.CategoryDAO;
import org.itson.ecommerce_e2.dao.ProductDAO;
import org.itson.ecommerce_e2.models.Category;
import org.itson.ecommerce_e2.models.Product;
import org.itson.ecommerce_e2.services.IProductService;

@ApplicationScoped
public class ProductService implements IProductService {

    @Inject
    private EntityManager em;
    @Inject
    private ProductDAO productDAO;
    @Inject
    private CategoryDAO categoryDAO;

    @Override
    public Product create(String name, String description, BigDecimal price,
            int stock, Long categoryId, String imageKey) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Categoría no encontrada: " + categoryId);
        }

        em.getTransaction().begin();
        try {
            Product product = new Product(name, description, price, stock, category);
            product.setImageKey(imageKey);
            productDAO.create(product);
            em.getTransaction().commit();
            return product;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Product findById(Long id) {
        return productDAO.findById(id);
    }

    @Override
    public List<Product> findAllActive() {
        return productDAO.findAllActive();
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return productDAO.findByCategory(categoryId);
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAllActive();
        }
        return productDAO.findByNameContaining(keyword.trim());
    }

    @Override
    public Product update(Product product) {
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }

        em.getTransaction().begin();
        try {
            Product updated = productDAO.update(product);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void deactivate(Long id) {
        em.getTransaction().begin();
        try {
            productDAO.deactivate(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Reduce el stock. Se llama desde OrderService durante el checkout. El
     * OrderService ya tiene la transacción abierta — este método solo ejecuta
     * la operación, no abre una nueva.
     */
    @Override
    public void reduceStock(Long productId, int quantity) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Producto no encontrado: " + productId);
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException(
                    "Stock insuficiente para '" + product.getName() + "'. "
                    + "Disponible: " + product.getStock() + ", solicitado: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        productDAO.update(product);
    }
}
