
package org.itson.ecommerce_e2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.itson.ecommerce_e2.models.Cart;
import org.itson.ecommerce_e2.models.Address;
import java.util.ArrayList;
import java.util.List;
import org.itson.ecommerce_e2.dao.exceptions.IllegalOrphanException;
import org.itson.ecommerce_e2.dao.exceptions.NonexistentEntityException;
import org.itson.ecommerce_e2.models.Order;
import org.itson.ecommerce_e2.models.PaymentMethod;
import org.itson.ecommerce_e2.models.Review;
import org.itson.ecommerce_e2.models.User;

/**
 *
 * @author gatog
 */
public class UserJpaController implements Serializable {

    public UserJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(User user) {
        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<Address>());
        }
        if (user.getOrders() == null) {
            user.setOrders(new ArrayList<Order>());
        }
        if (user.getPaymentMethods() == null) {
            user.setPaymentMethods(new ArrayList<PaymentMethod>());
        }
        if (user.getReviews() == null) {
            user.setReviews(new ArrayList<Review>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cart cart = user.getCart();
            if (cart != null) {
                cart = em.getReference(cart.getClass(), cart.getId());
                user.setCart(cart);
            }
            List<Address> attachedAddresses = new ArrayList<Address>();
            for (Address addressesAddressToAttach : user.getAddresses()) {
                addressesAddressToAttach = em.getReference(addressesAddressToAttach.getClass(), addressesAddressToAttach.getId());
                attachedAddresses.add(addressesAddressToAttach);
            }
            user.setAddresses(attachedAddresses);
            List<Order> attachedOrders = new ArrayList<Order>();
            for (Order ordersOrderToAttach : user.getOrders()) {
                ordersOrderToAttach = em.getReference(ordersOrderToAttach.getClass(), ordersOrderToAttach.getId());
                attachedOrders.add(ordersOrderToAttach);
            }
            user.setOrders(attachedOrders);
            List<PaymentMethod> attachedPaymentMethods = new ArrayList<PaymentMethod>();
            for (PaymentMethod paymentMethodsPaymentMethodToAttach : user.getPaymentMethods()) {
                paymentMethodsPaymentMethodToAttach = em.getReference(paymentMethodsPaymentMethodToAttach.getClass(), paymentMethodsPaymentMethodToAttach.getId());
                attachedPaymentMethods.add(paymentMethodsPaymentMethodToAttach);
            }
            user.setPaymentMethods(attachedPaymentMethods);
            List<Review> attachedReviews = new ArrayList<Review>();
            for (Review reviewsReviewToAttach : user.getReviews()) {
                reviewsReviewToAttach = em.getReference(reviewsReviewToAttach.getClass(), reviewsReviewToAttach.getId());
                attachedReviews.add(reviewsReviewToAttach);
            }
            user.setReviews(attachedReviews);
            em.persist(user);
            if (cart != null) {
                User oldUserOfCart = cart.getUser();
                if (oldUserOfCart != null) {
                    oldUserOfCart.setCart(null);
                    oldUserOfCart = em.merge(oldUserOfCart);
                }
                cart.setUser(user);
                cart = em.merge(cart);
            }
            for (Address addressesAddress : user.getAddresses()) {
                User oldUserOfAddressesAddress = addressesAddress.getUser();
                addressesAddress.setUser(user);
                addressesAddress = em.merge(addressesAddress);
                if (oldUserOfAddressesAddress != null) {
                    oldUserOfAddressesAddress.getAddresses().remove(addressesAddress);
                    oldUserOfAddressesAddress = em.merge(oldUserOfAddressesAddress);
                }
            }
            for (Order ordersOrder : user.getOrders()) {
                User oldUserOfOrdersOrder = ordersOrder.getUser();
                ordersOrder.setUser(user);
                ordersOrder = em.merge(ordersOrder);
                if (oldUserOfOrdersOrder != null) {
                    oldUserOfOrdersOrder.getOrders().remove(ordersOrder);
                    oldUserOfOrdersOrder = em.merge(oldUserOfOrdersOrder);
                }
            }
            for (PaymentMethod paymentMethodsPaymentMethod : user.getPaymentMethods()) {
                User oldUserOfPaymentMethodsPaymentMethod = paymentMethodsPaymentMethod.getUser();
                paymentMethodsPaymentMethod.setUser(user);
                paymentMethodsPaymentMethod = em.merge(paymentMethodsPaymentMethod);
                if (oldUserOfPaymentMethodsPaymentMethod != null) {
                    oldUserOfPaymentMethodsPaymentMethod.getPaymentMethods().remove(paymentMethodsPaymentMethod);
                    oldUserOfPaymentMethodsPaymentMethod = em.merge(oldUserOfPaymentMethodsPaymentMethod);
                }
            }
            for (Review reviewsReview : user.getReviews()) {
                User oldUserOfReviewsReview = reviewsReview.getUser();
                reviewsReview.setUser(user);
                reviewsReview = em.merge(reviewsReview);
                if (oldUserOfReviewsReview != null) {
                    oldUserOfReviewsReview.getReviews().remove(reviewsReview);
                    oldUserOfReviewsReview = em.merge(oldUserOfReviewsReview);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(User user) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User persistentUser = em.find(User.class, user.getId());
            Cart cartOld = persistentUser.getCart();
            Cart cartNew = user.getCart();
            List<Address> addressesOld = persistentUser.getAddresses();
            List<Address> addressesNew = user.getAddresses();
            List<Order> ordersOld = persistentUser.getOrders();
            List<Order> ordersNew = user.getOrders();
            List<PaymentMethod> paymentMethodsOld = persistentUser.getPaymentMethods();
            List<PaymentMethod> paymentMethodsNew = user.getPaymentMethods();
            List<Review> reviewsOld = persistentUser.getReviews();
            List<Review> reviewsNew = user.getReviews();
            List<String> illegalOrphanMessages = null;
            if (cartOld != null && !cartOld.equals(cartNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Cart " + cartOld + " since its user field is not nullable.");
            }
            for (Address addressesOldAddress : addressesOld) {
                if (!addressesNew.contains(addressesOldAddress)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Address " + addressesOldAddress + " since its user field is not nullable.");
                }
            }
            for (Order ordersOldOrder : ordersOld) {
                if (!ordersNew.contains(ordersOldOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Order " + ordersOldOrder + " since its user field is not nullable.");
                }
            }
            for (PaymentMethod paymentMethodsOldPaymentMethod : paymentMethodsOld) {
                if (!paymentMethodsNew.contains(paymentMethodsOldPaymentMethod)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PaymentMethod " + paymentMethodsOldPaymentMethod + " since its user field is not nullable.");
                }
            }
            for (Review reviewsOldReview : reviewsOld) {
                if (!reviewsNew.contains(reviewsOldReview)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Review " + reviewsOldReview + " since its user field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (cartNew != null) {
                cartNew = em.getReference(cartNew.getClass(), cartNew.getId());
                user.setCart(cartNew);
            }
            List<Address> attachedAddressesNew = new ArrayList<Address>();
            for (Address addressesNewAddressToAttach : addressesNew) {
                addressesNewAddressToAttach = em.getReference(addressesNewAddressToAttach.getClass(), addressesNewAddressToAttach.getId());
                attachedAddressesNew.add(addressesNewAddressToAttach);
            }
            addressesNew = attachedAddressesNew;
            user.setAddresses(addressesNew);
            List<Order> attachedOrdersNew = new ArrayList<Order>();
            for (Order ordersNewOrderToAttach : ordersNew) {
                ordersNewOrderToAttach = em.getReference(ordersNewOrderToAttach.getClass(), ordersNewOrderToAttach.getId());
                attachedOrdersNew.add(ordersNewOrderToAttach);
            }
            ordersNew = attachedOrdersNew;
            user.setOrders(ordersNew);
            List<PaymentMethod> attachedPaymentMethodsNew = new ArrayList<PaymentMethod>();
            for (PaymentMethod paymentMethodsNewPaymentMethodToAttach : paymentMethodsNew) {
                paymentMethodsNewPaymentMethodToAttach = em.getReference(paymentMethodsNewPaymentMethodToAttach.getClass(), paymentMethodsNewPaymentMethodToAttach.getId());
                attachedPaymentMethodsNew.add(paymentMethodsNewPaymentMethodToAttach);
            }
            paymentMethodsNew = attachedPaymentMethodsNew;
            user.setPaymentMethods(paymentMethodsNew);
            List<Review> attachedReviewsNew = new ArrayList<Review>();
            for (Review reviewsNewReviewToAttach : reviewsNew) {
                reviewsNewReviewToAttach = em.getReference(reviewsNewReviewToAttach.getClass(), reviewsNewReviewToAttach.getId());
                attachedReviewsNew.add(reviewsNewReviewToAttach);
            }
            reviewsNew = attachedReviewsNew;
            user.setReviews(reviewsNew);
            user = em.merge(user);
            if (cartNew != null && !cartNew.equals(cartOld)) {
                User oldUserOfCart = cartNew.getUser();
                if (oldUserOfCart != null) {
                    oldUserOfCart.setCart(null);
                    oldUserOfCart = em.merge(oldUserOfCart);
                }
                cartNew.setUser(user);
                cartNew = em.merge(cartNew);
            }
            for (Address addressesNewAddress : addressesNew) {
                if (!addressesOld.contains(addressesNewAddress)) {
                    User oldUserOfAddressesNewAddress = addressesNewAddress.getUser();
                    addressesNewAddress.setUser(user);
                    addressesNewAddress = em.merge(addressesNewAddress);
                    if (oldUserOfAddressesNewAddress != null && !oldUserOfAddressesNewAddress.equals(user)) {
                        oldUserOfAddressesNewAddress.getAddresses().remove(addressesNewAddress);
                        oldUserOfAddressesNewAddress = em.merge(oldUserOfAddressesNewAddress);
                    }
                }
            }
            for (Order ordersNewOrder : ordersNew) {
                if (!ordersOld.contains(ordersNewOrder)) {
                    User oldUserOfOrdersNewOrder = ordersNewOrder.getUser();
                    ordersNewOrder.setUser(user);
                    ordersNewOrder = em.merge(ordersNewOrder);
                    if (oldUserOfOrdersNewOrder != null && !oldUserOfOrdersNewOrder.equals(user)) {
                        oldUserOfOrdersNewOrder.getOrders().remove(ordersNewOrder);
                        oldUserOfOrdersNewOrder = em.merge(oldUserOfOrdersNewOrder);
                    }
                }
            }
            for (PaymentMethod paymentMethodsNewPaymentMethod : paymentMethodsNew) {
                if (!paymentMethodsOld.contains(paymentMethodsNewPaymentMethod)) {
                    User oldUserOfPaymentMethodsNewPaymentMethod = paymentMethodsNewPaymentMethod.getUser();
                    paymentMethodsNewPaymentMethod.setUser(user);
                    paymentMethodsNewPaymentMethod = em.merge(paymentMethodsNewPaymentMethod);
                    if (oldUserOfPaymentMethodsNewPaymentMethod != null && !oldUserOfPaymentMethodsNewPaymentMethod.equals(user)) {
                        oldUserOfPaymentMethodsNewPaymentMethod.getPaymentMethods().remove(paymentMethodsNewPaymentMethod);
                        oldUserOfPaymentMethodsNewPaymentMethod = em.merge(oldUserOfPaymentMethodsNewPaymentMethod);
                    }
                }
            }
            for (Review reviewsNewReview : reviewsNew) {
                if (!reviewsOld.contains(reviewsNewReview)) {
                    User oldUserOfReviewsNewReview = reviewsNewReview.getUser();
                    reviewsNewReview.setUser(user);
                    reviewsNewReview = em.merge(reviewsNewReview);
                    if (oldUserOfReviewsNewReview != null && !oldUserOfReviewsNewReview.equals(user)) {
                        oldUserOfReviewsNewReview.getReviews().remove(reviewsNewReview);
                        oldUserOfReviewsNewReview = em.merge(oldUserOfReviewsNewReview);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = user.getId();
                if (findUser(id) == null) {
                    throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
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
            User user;
            try {
                user = em.getReference(User.class, id);
                user.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Cart cartOrphanCheck = user.getCart();
            if (cartOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Cart " + cartOrphanCheck + " in its cart field has a non-nullable user field.");
            }
            List<Address> addressesOrphanCheck = user.getAddresses();
            for (Address addressesOrphanCheckAddress : addressesOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Address " + addressesOrphanCheckAddress + " in its addresses field has a non-nullable user field.");
            }
            List<Order> ordersOrphanCheck = user.getOrders();
            for (Order ordersOrphanCheckOrder : ordersOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Order " + ordersOrphanCheckOrder + " in its orders field has a non-nullable user field.");
            }
            List<PaymentMethod> paymentMethodsOrphanCheck = user.getPaymentMethods();
            for (PaymentMethod paymentMethodsOrphanCheckPaymentMethod : paymentMethodsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the PaymentMethod " + paymentMethodsOrphanCheckPaymentMethod + " in its paymentMethods field has a non-nullable user field.");
            }
            List<Review> reviewsOrphanCheck = user.getReviews();
            for (Review reviewsOrphanCheckReview : reviewsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Review " + reviewsOrphanCheckReview + " in its reviews field has a non-nullable user field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(user);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<User> findUserEntities() {
        return findUserEntities(true, -1, -1);
    }

    public List<User> findUserEntities(int maxResults, int firstResult) {
        return findUserEntities(false, maxResults, firstResult);
    }

    private List<User> findUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(User.class));
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

    public User findUser(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public int getUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<User> rt = cq.from(User.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
