package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.itson.ecommerce_e2.models.OrderItem;

@ApplicationScoped
public class OrderItemDAO {

    @Inject
    private EntityManager em;

    public void create(OrderItem item) {
        em.persist(item);
    }

    public OrderItem findById(Long id) {
        return em.find(OrderItem.class, id);
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        return em.createQuery(
                "SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId",
                OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public OrderItem update(OrderItem item) {
        return em.merge(item);
    }
}
