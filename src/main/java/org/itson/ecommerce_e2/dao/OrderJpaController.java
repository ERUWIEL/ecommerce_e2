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
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.Address;
import org.itson.ecommerce_e2.models.PaymentMethod;
import org.itson.ecommerce_e2.models.OrderItem;
import java.util.ArrayList;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.IllegalOrphanException;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.Order;

/**
 *
 * @author gatog
 */
public class OrderJpaController implements Serializable {

    public OrderJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Order order) {
        if (order.getItems() == null) {
            order.setItems(new ArrayList<OrderItem>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User user = order.getUser();
            if (user != null) {
                user = em.getReference(user.getClass(), user.getId());
                order.setUser(user);
            }
            Address address = order.getAddress();
            if (address != null) {
                address = em.getReference(address.getClass(), address.getId());
                order.setAddress(address);
            }
            PaymentMethod paymentMethod = order.getPaymentMethod();
            if (paymentMethod != null) {
                paymentMethod = em.getReference(paymentMethod.getClass(), paymentMethod.getId());
                order.setPaymentMethod(paymentMethod);
            }
            List<OrderItem> attachedItems = new ArrayList<OrderItem>();
            for (OrderItem itemsOrderItemToAttach : order.getItems()) {
                itemsOrderItemToAttach = em.getReference(itemsOrderItemToAttach.getClass(), itemsOrderItemToAttach.getId());
                attachedItems.add(itemsOrderItemToAttach);
            }
            order.setItems(attachedItems);
            em.persist(order);
            if (user != null) {
                user.getOrders().add(order);
                user = em.merge(user);
            }
            if (address != null) {
                address.getOrders().add(order);
                address = em.merge(address);
            }
            if (paymentMethod != null) {
                paymentMethod.getOrders().add(order);
                paymentMethod = em.merge(paymentMethod);
            }
            for (OrderItem itemsOrderItem : order.getItems()) {
                Order oldOrderOfItemsOrderItem = itemsOrderItem.getOrder();
                itemsOrderItem.setOrder(order);
                itemsOrderItem = em.merge(itemsOrderItem);
                if (oldOrderOfItemsOrderItem != null) {
                    oldOrderOfItemsOrderItem.getItems().remove(itemsOrderItem);
                    oldOrderOfItemsOrderItem = em.merge(oldOrderOfItemsOrderItem);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Order order) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Order persistentOrder = em.find(Order.class, order.getId());
            User userOld = persistentOrder.getUser();
            User userNew = order.getUser();
            Address addressOld = persistentOrder.getAddress();
            Address addressNew = order.getAddress();
            PaymentMethod paymentMethodOld = persistentOrder.getPaymentMethod();
            PaymentMethod paymentMethodNew = order.getPaymentMethod();
            List<OrderItem> itemsOld = persistentOrder.getItems();
            List<OrderItem> itemsNew = order.getItems();
            List<String> illegalOrphanMessages = null;
            for (OrderItem itemsOldOrderItem : itemsOld) {
                if (!itemsNew.contains(itemsOldOrderItem)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain OrderItem " + itemsOldOrderItem + " since its order field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userNew != null) {
                userNew = em.getReference(userNew.getClass(), userNew.getId());
                order.setUser(userNew);
            }
            if (addressNew != null) {
                addressNew = em.getReference(addressNew.getClass(), addressNew.getId());
                order.setAddress(addressNew);
            }
            if (paymentMethodNew != null) {
                paymentMethodNew = em.getReference(paymentMethodNew.getClass(), paymentMethodNew.getId());
                order.setPaymentMethod(paymentMethodNew);
            }
            List<OrderItem> attachedItemsNew = new ArrayList<OrderItem>();
            for (OrderItem itemsNewOrderItemToAttach : itemsNew) {
                itemsNewOrderItemToAttach = em.getReference(itemsNewOrderItemToAttach.getClass(), itemsNewOrderItemToAttach.getId());
                attachedItemsNew.add(itemsNewOrderItemToAttach);
            }
            itemsNew = attachedItemsNew;
            order.setItems(itemsNew);
            order = em.merge(order);
            if (userOld != null && !userOld.equals(userNew)) {
                userOld.getOrders().remove(order);
                userOld = em.merge(userOld);
            }
            if (userNew != null && !userNew.equals(userOld)) {
                userNew.getOrders().add(order);
                userNew = em.merge(userNew);
            }
            if (addressOld != null && !addressOld.equals(addressNew)) {
                addressOld.getOrders().remove(order);
                addressOld = em.merge(addressOld);
            }
            if (addressNew != null && !addressNew.equals(addressOld)) {
                addressNew.getOrders().add(order);
                addressNew = em.merge(addressNew);
            }
            if (paymentMethodOld != null && !paymentMethodOld.equals(paymentMethodNew)) {
                paymentMethodOld.getOrders().remove(order);
                paymentMethodOld = em.merge(paymentMethodOld);
            }
            if (paymentMethodNew != null && !paymentMethodNew.equals(paymentMethodOld)) {
                paymentMethodNew.getOrders().add(order);
                paymentMethodNew = em.merge(paymentMethodNew);
            }
            for (OrderItem itemsNewOrderItem : itemsNew) {
                if (!itemsOld.contains(itemsNewOrderItem)) {
                    Order oldOrderOfItemsNewOrderItem = itemsNewOrderItem.getOrder();
                    itemsNewOrderItem.setOrder(order);
                    itemsNewOrderItem = em.merge(itemsNewOrderItem);
                    if (oldOrderOfItemsNewOrderItem != null && !oldOrderOfItemsNewOrderItem.equals(order)) {
                        oldOrderOfItemsNewOrderItem.getItems().remove(itemsNewOrderItem);
                        oldOrderOfItemsNewOrderItem = em.merge(oldOrderOfItemsNewOrderItem);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = order.getId();
                if (findOrder(id) == null) {
                    throw new NonexistentEntityException("The order with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Order order;
            try {
                order = em.getReference(Order.class, id);
                order.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The order with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<OrderItem> itemsOrphanCheck = order.getItems();
            for (OrderItem itemsOrphanCheckOrderItem : itemsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Order (" + order + ") cannot be destroyed since the OrderItem " + itemsOrphanCheckOrderItem + " in its items field has a non-nullable order field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            User user = order.getUser();
            if (user != null) {
                user.getOrders().remove(order);
                user = em.merge(user);
            }
            Address address = order.getAddress();
            if (address != null) {
                address.getOrders().remove(order);
                address = em.merge(address);
            }
            PaymentMethod paymentMethod = order.getPaymentMethod();
            if (paymentMethod != null) {
                paymentMethod.getOrders().remove(order);
                paymentMethod = em.merge(paymentMethod);
            }
            em.remove(order);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Order> findOrderEntities() {
        return findOrderEntities(true, -1, -1);
    }

    public List<Order> findOrderEntities(int maxResults, int firstResult) {
        return findOrderEntities(false, maxResults, firstResult);
    }

    private List<Order> findOrderEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Order.class));
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

    public Order findOrder(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Order.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrderCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Order> rt = cq.from(Order.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
