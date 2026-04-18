/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.itson.ecommerce_e2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.Order;
import org.itson.ecommerce_e2.models.OrderItem;
import org.itson.ecommerce_e2.models.Product;

/**
 *
 * @author gatog
 */
public class OrderItemJpaController implements Serializable {

    public OrderItemJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(OrderItem orderItem) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Order order = orderItem.getOrder();
            if (order != null) {
                order = em.getReference(order.getClass(), order.getId());
                orderItem.setOrder(order);
            }
            Product product = orderItem.getProduct();
            if (product != null) {
                product = em.getReference(product.getClass(), product.getId());
                orderItem.setProduct(product);
            }
            em.persist(orderItem);
            if (order != null) {
                order.getItems().add(orderItem);
                order = em.merge(order);
            }
            if (product != null) {
                product.getOrderItems().add(orderItem);
                product = em.merge(product);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(OrderItem orderItem) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            OrderItem persistentOrderItem = em.find(OrderItem.class, orderItem.getId());
            Order orderOld = persistentOrderItem.getOrder();
            Order orderNew = orderItem.getOrder();
            Product productOld = persistentOrderItem.getProduct();
            Product productNew = orderItem.getProduct();
            if (orderNew != null) {
                orderNew = em.getReference(orderNew.getClass(), orderNew.getId());
                orderItem.setOrder(orderNew);
            }
            if (productNew != null) {
                productNew = em.getReference(productNew.getClass(), productNew.getId());
                orderItem.setProduct(productNew);
            }
            orderItem = em.merge(orderItem);
            if (orderOld != null && !orderOld.equals(orderNew)) {
                orderOld.getItems().remove(orderItem);
                orderOld = em.merge(orderOld);
            }
            if (orderNew != null && !orderNew.equals(orderOld)) {
                orderNew.getItems().add(orderItem);
                orderNew = em.merge(orderNew);
            }
            if (productOld != null && !productOld.equals(productNew)) {
                productOld.getOrderItems().remove(orderItem);
                productOld = em.merge(productOld);
            }
            if (productNew != null && !productNew.equals(productOld)) {
                productNew.getOrderItems().add(orderItem);
                productNew = em.merge(productNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = orderItem.getId();
                if (findOrderItem(id) == null) {
                    throw new NonexistentEntityException("The orderItem with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            OrderItem orderItem;
            try {
                orderItem = em.getReference(OrderItem.class, id);
                orderItem.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The orderItem with id " + id + " no longer exists.", enfe);
            }
            Order order = orderItem.getOrder();
            if (order != null) {
                order.getItems().remove(orderItem);
                order = em.merge(order);
            }
            Product product = orderItem.getProduct();
            if (product != null) {
                product.getOrderItems().remove(orderItem);
                product = em.merge(product);
            }
            em.remove(orderItem);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<OrderItem> findOrderItemEntities() {
        return findOrderItemEntities(true, -1, -1);
    }

    public List<OrderItem> findOrderItemEntities(int maxResults, int firstResult) {
        return findOrderItemEntities(false, maxResults, firstResult);
    }

    private List<OrderItem> findOrderItemEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(OrderItem.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public OrderItem findOrderItem(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(OrderItem.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrderItemCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<OrderItem> rt = cq.from(OrderItem.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
