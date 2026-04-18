package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.models.Product;

@ApplicationScoped
public class ProductDAO {

    @Inject
    private EntityManager em;

    public void create(Product product) {
        em.persist(product);
    }

    public Product findById(Long id) {
        return em.find(Product.class, id);
    }

    /**
     * Solo productos activos — para el catálogo público.
     *
     * @return
     */
    public List<Product> findAllActive() {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.active = true ORDER BY p.name",
                Product.class)
                .getResultList();
    }

    public List<Product> findAll() {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.name", Product.class)
                .getResultList();
    }

    public List<Product> findByCategory(Long categoryId) {
        return em.createQuery(
                "SELECT p FROM Product p "
                + "WHERE p.category.id = :catId AND p.active = true "
                + "ORDER BY p.name",
                Product.class)
                .setParameter("catId", categoryId)
                .getResultList();
    }

    /**
     * Búsqueda por nombre — útil para el buscador.
     *
     * @param keyword
     * @return
     */
    public List<Product> findByNameContaining(String keyword) {
        return em.createQuery(
                "SELECT p FROM Product p "
                + "WHERE LOWER(p.name) LIKE LOWER(:kw) AND p.active = true",
                Product.class)
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
    }

    public Product update(Product product) {
        return em.merge(product);
    }

    /**
     * Soft delete — marca el producto como inactivo en lugar de borrarlo.
     *
     * @param id
     */
    public void deactivate(Long id) {
        Product p = findById(id);
        if (p != null) {
            p.setActive(false);
            em.merge(p);
        }
    }

    public int countActive() {
        return ((Long) em.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.active = true")
                .getSingleResult()).intValue();
    }
}
