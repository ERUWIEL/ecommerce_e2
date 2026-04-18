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
import org.itson.ecommerce_e2.models.Address;

/**
 *
 * @author gatog
 */
public class AddressJpaController implements Serializable {

    public AddressJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Address address) {
        if (address.getOrders() == null) {
            address.setOrders(new ArrayList<Order>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User user = address.getUser();
            if (user != null) {
                user = em.getReference(user.getClass(), user.getId());
                address.setUser(user);
            }
            List<Order> attachedOrders = new ArrayList<Order>();
            for (Order ordersOrderToAttach : address.getOrders()) {
                ordersOrderToAttach = em.getReference(ordersOrderToAttach.getClass(), ordersOrderToAttach.getId());
                attachedOrders.add(ordersOrderToAttach);
            }
            address.setOrders(attachedOrders);
            em.persist(address);
            if (user != null) {
                user.getAddresses().add(address);
                user = em.merge(user);
            }
            for (Order ordersOrder : address.getOrders()) {
                Address oldAddressOfOrdersOrder = ordersOrder.getAddress();
                ordersOrder.setAddress(address);
                ordersOrder = em.merge(ordersOrder);
                if (oldAddressOfOrdersOrder != null) {
                    oldAddressOfOrdersOrder.getOrders().remove(ordersOrder);
                    oldAddressOfOrdersOrder = em.merge(oldAddressOfOrdersOrder);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Address address) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Address persistentAddress = em.find(Address.class, address.getId());
            User userOld = persistentAddress.getUser();
            User userNew = address.getUser();
            List<Order> ordersOld = persistentAddress.getOrders();
            List<Order> ordersNew = address.getOrders();
            List<String> illegalOrphanMessages = null;
            for (Order ordersOldOrder : ordersOld) {
                if (!ordersNew.contains(ordersOldOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Order " + ordersOldOrder + " since its address field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userNew != null) {
                userNew = em.getReference(userNew.getClass(), userNew.getId());
                address.setUser(userNew);
            }
            List<Order> attachedOrdersNew = new ArrayList<Order>();
            for (Order ordersNewOrderToAttach : ordersNew) {
                ordersNewOrderToAttach = em.getReference(ordersNewOrderToAttach.getClass(), ordersNewOrderToAttach.getId());
                attachedOrdersNew.add(ordersNewOrderToAttach);
            }
            ordersNew = attachedOrdersNew;
            address.setOrders(ordersNew);
            address = em.merge(address);
            if (userOld != null && !userOld.equals(userNew)) {
                userOld.getAddresses().remove(address);
                userOld = em.merge(userOld);
            }
            if (userNew != null && !userNew.equals(userOld)) {
                userNew.getAddresses().add(address);
                userNew = em.merge(userNew);
            }
            for (Order ordersNewOrder : ordersNew) {
                if (!ordersOld.contains(ordersNewOrder)) {
                    Address oldAddressOfOrdersNewOrder = ordersNewOrder.getAddress();
                    ordersNewOrder.setAddress(address);
                    ordersNewOrder = em.merge(ordersNewOrder);
                    if (oldAddressOfOrdersNewOrder != null && !oldAddressOfOrdersNewOrder.equals(address)) {
                        oldAddressOfOrdersNewOrder.getOrders().remove(ordersNewOrder);
                        oldAddressOfOrdersNewOrder = em.merge(oldAddressOfOrdersNewOrder);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = address.getId();
                if (findAddress(id) == null) {
                    throw new NonexistentEntityException("The address with id " + id + " no longer exists.");
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
            Address address;
            try {
                address = em.getReference(Address.class, id);
                address.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The address with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Order> ordersOrphanCheck = address.getOrders();
            for (Order ordersOrphanCheckOrder : ordersOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Address (" + address + ") cannot be destroyed since the Order " + ordersOrphanCheckOrder + " in its orders field has a non-nullable address field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            User user = address.getUser();
            if (user != null) {
                user.getAddresses().remove(address);
                user = em.merge(user);
            }
            em.remove(address);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Address> findAddressEntities() {
        return findAddressEntities(true, -1, -1);
    }

    public List<Address> findAddressEntities(int maxResults, int firstResult) {
        return findAddressEntities(false, maxResults, firstResult);
    }

    private List<Address> findAddressEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Address.class));
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

    public Address findAddress(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Address.class, id);
        } finally {
            em.close();
        }
    }

    public int getAddressCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Address> rt = cq.from(Address.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
