package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.itson.ecommerce_e2.models.Category;

@ApplicationScoped
public class CategoryDAO {

    @Inject
    private EntityManager em;

    public void create(Category category) {
        em.persist(category);
    }

    public Category findById(Long id) {
        return em.find(Category.class, id);
    }

    public List<Category> findAll() {
        return em.createQuery(
                "SELECT c FROM Category c ORDER BY c.name",
                Category.class)
                .getResultList();
    }

    public Category findByName(String name) {
        try {
            return em.createQuery(
                    "SELECT c FROM Category c WHERE c.name = :name",
                    Category.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean existsByName(String name) {
        return findByName(name) != null;
    }

    public Category update(Category category) {
        return em.merge(category);
    }

    public void delete(Long id) {
        Category c = findById(id);
        if (c != null) {
            em.remove(c);
        }
    }
}
