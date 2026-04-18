package org.itson.ecommerce_e2.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.itson.ecommerce_e2.models.PaymentMethod;

@ApplicationScoped
public class PaymentMethodDAO {

    @Inject
    private EntityManager em;

    public void create(PaymentMethod paymentMethod) {
        em.persist(paymentMethod);
    }

    public PaymentMethod findById(Long id) {
        return em.find(PaymentMethod.class, id);
    }

    public List<PaymentMethod> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT pm FROM PaymentMethod pm WHERE pm.user.id = :userId",
                PaymentMethod.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Obtiene el método de pago predeterminado del usuario. Se pre-selecciona
     * en el checkout igual que la dirección default.
     * @param userId
     * @return 
     */
    public PaymentMethod findDefaultByUserId(Long userId) {
        try {
            return em.createQuery(
                    "SELECT pm FROM PaymentMethod pm "
                    + "WHERE pm.user.id = :userId AND pm.defaultMethod = true",
                    PaymentMethod.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public PaymentMethod update(PaymentMethod paymentMethod) {
        return em.merge(paymentMethod);
    }

    public void delete(Long id) {
        PaymentMethod pm = findById(id);
        if (pm != null) {
            em.remove(pm);
        }
    }
}
