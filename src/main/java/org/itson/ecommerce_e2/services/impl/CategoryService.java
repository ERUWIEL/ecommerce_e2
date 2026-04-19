package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.dao.CategoryDAO;
import org.itson.ecommerce_e2.models.Category;
import org.itson.ecommerce_e2.services.ICategoryService;

@ApplicationScoped
public class CategoryService implements ICategoryService {

    @Inject
    private EntityManager em;
    @Inject
    private CategoryDAO categoryDAO;

    @Override
    public Category create(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        if (categoryDAO.existsByName(name)) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre.");
        }

        em.getTransaction().begin();
        try {
            Category category = new Category(name, description);
            categoryDAO.create(category);
            em.getTransaction().commit();
            return category;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Category findById(Long id) {
        return categoryDAO.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryDAO.findAll();
    }

    @Override
    public Category update(Category category) {
        em.getTransaction().begin();
        try {
            Category updated = categoryDAO.update(category);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        Category category = categoryDAO.findById(id);
        if (category == null) {
            throw new IllegalArgumentException("Categoría no encontrada: " + id);
        }
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar una categoría con productos activos.");
        }

        em.getTransaction().begin();
        try {
            categoryDAO.delete(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
