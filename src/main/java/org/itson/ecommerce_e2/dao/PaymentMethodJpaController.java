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
import org.itson.ecommerce_e2.models.Order;
import java.util.ArrayList;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.IllegalOrphanException;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.PaymentMethod;

/**
 *
 * @author gatog
 */
public class PaymentMethodJpaController implements Serializable {

    public PaymentMethodJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PaymentMethod paymentMethod) {
        if (paymentMethod.getOrders() == null) {
            paymentMethod.setOrders(new ArrayList<Order>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User user = paymentMethod.getUser();
            if (user != null) {
                user = em.getReference(user.getClass(), user.getId());
                paymentMethod.setUser(user);
            }
            List<Order> attachedOrders = new ArrayList<Order>();
            for (Order ordersOrderToAttach : paymentMethod.getOrders()) {
                ordersOrderToAttach = em.getReference(ordersOrderToAttach.getClass(), ordersOrderToAttach.getId());
                attachedOrders.add(ordersOrderToAttach);
            }
            paymentMethod.setOrders(attachedOrders);
            em.persist(paymentMethod);
            if (user != null) {
                user.getPaymentMethods().add(paymentMethod);
                user = em.merge(user);
            }
            for (Order ordersOrder : paymentMethod.getOrders()) {
                PaymentMethod oldPaymentMethodOfOrdersOrder = ordersOrder.getPaymentMethod();
                ordersOrder.setPaymentMethod(paymentMethod);
                ordersOrder = em.merge(ordersOrder);
                if (oldPaymentMethodOfOrdersOrder != null) {
                    oldPaymentMethodOfOrdersOrder.getOrders().remove(ordersOrder);
                    oldPaymentMethodOfOrdersOrder = em.merge(oldPaymentMethodOfOrdersOrder);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PaymentMethod paymentMethod) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PaymentMethod persistentPaymentMethod = em.find(PaymentMethod.class, paymentMethod.getId());
            User userOld = persistentPaymentMethod.getUser();
            User userNew = paymentMethod.getUser();
            List<Order> ordersOld = persistentPaymentMethod.getOrders();
            List<Order> ordersNew = paymentMethod.getOrders();
            List<String> illegalOrphanMessages = null;
            for (Order ordersOldOrder : ordersOld) {
                if (!ordersNew.contains(ordersOldOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Order " + ordersOldOrder + " since its paymentMethod field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userNew != null) {
                userNew = em.getReference(userNew.getClass(), userNew.getId());
                paymentMethod.setUser(userNew);
            }
            List<Order> attachedOrdersNew = new ArrayList<Order>();
            for (Order ordersNewOrderToAttach : ordersNew) {
                ordersNewOrderToAttach = em.getReference(ordersNewOrderToAttach.getClass(), ordersNewOrderToAttach.getId());
                attachedOrdersNew.add(ordersNewOrderToAttach);
            }
            ordersNew = attachedOrdersNew;
            paymentMethod.setOrders(ordersNew);
            paymentMethod = em.merge(paymentMethod);
            if (userOld != null && !userOld.equals(userNew)) {
                userOld.getPaymentMethods().remove(paymentMethod);
                userOld = em.merge(userOld);
            }
            if (userNew != null && !userNew.equals(userOld)) {
                userNew.getPaymentMethods().add(paymentMethod);
                userNew = em.merge(userNew);
            }
            for (Order ordersNewOrder : ordersNew) {
                if (!ordersOld.contains(ordersNewOrder)) {
                    PaymentMethod oldPaymentMethodOfOrdersNewOrder = ordersNewOrder.getPaymentMethod();
                    ordersNewOrder.setPaymentMethod(paymentMethod);
                    ordersNewOrder = em.merge(ordersNewOrder);
                    if (oldPaymentMethodOfOrdersNewOrder != null && !oldPaymentMethodOfOrdersNewOrder.equals(paymentMethod)) {
                        oldPaymentMethodOfOrdersNewOrder.getOrders().remove(ordersNewOrder);
                        oldPaymentMethodOfOrdersNewOrder = em.merge(oldPaymentMethodOfOrdersNewOrder);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = paymentMethod.getId();
                if (findPaymentMethod(id) == null) {
                    throw new NonexistentEntityException("The paymentMethod with id " + id + " no longer exists.");
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
            PaymentMethod paymentMethod;
            try {
                paymentMethod = em.getReference(PaymentMethod.class, id);
                paymentMethod.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The paymentMethod with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Order> ordersOrphanCheck = paymentMethod.getOrders();
            for (Order ordersOrphanCheckOrder : ordersOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This PaymentMethod (" + paymentMethod + ") cannot be destroyed since the Order " + ordersOrphanCheckOrder + " in its orders field has a non-nullable paymentMethod field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            User user = paymentMethod.getUser();
            if (user != null) {
                user.getPaymentMethods().remove(paymentMethod);
                user = em.merge(user);
            }
            em.remove(paymentMethod);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PaymentMethod> findPaymentMethodEntities() {
        return findPaymentMethodEntities(true, -1, -1);
    }

    public List<PaymentMethod> findPaymentMethodEntities(int maxResults, int firstResult) {
        return findPaymentMethodEntities(false, maxResults, firstResult);
    }

    private List<PaymentMethod> findPaymentMethodEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PaymentMethod.class));
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

    public PaymentMethod findPaymentMethod(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PaymentMethod.class, id);
        } finally {
            em.close();
        }
    }

    public int getPaymentMethodCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PaymentMethod> rt = cq.from(PaymentMethod.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
