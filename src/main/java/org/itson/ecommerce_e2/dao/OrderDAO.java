package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.models.Order;
import org.itson.ecommerce_e2.models.enums.OrderStatus;

@ApplicationScoped
public class OrderDAO {

    @Inject
    private EntityManager em;

    public void create(Order order) {
        em.persist(order);
    }

    public Order findById(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC",
                Order.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return em.createQuery(
                "SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC",
                Order.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * Historial completo — para el panel de administración.
     * @return 
     */
    public List<Order> findAll() {
        return em.createQuery(
                "SELECT o FROM Order o ORDER BY o.createdAt DESC",
                Order.class)
                .getResultList();
    }

    public Order update(Order order) {
        return em.merge(order);
    }
}
