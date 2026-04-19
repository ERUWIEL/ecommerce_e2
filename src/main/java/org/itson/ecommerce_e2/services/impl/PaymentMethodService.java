package org.itson.ecommerce_e2.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.itson.ecommerce_e2.dao.PaymentMethodDAO;
import org.itson.ecommerce_e2.dao.UserDAO;
import org.itson.ecommerce_e2.models.PaymentMethod;
import org.itson.ecommerce_e2.models.User;
import org.itson.ecommerce_e2.models.enums.PaymentCategory;
import org.itson.ecommerce_e2.services.IPaymentMethodService;

@ApplicationScoped
public class PaymentMethodService implements IPaymentMethodService {

    @Inject
    private EntityManager em;
    @Inject
    private PaymentMethodDAO paymentMethodDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public PaymentMethod add(Long userId, String alias, String lastFourDigits,
            String cardholderName, LocalDate expirationDate,
            PaymentCategory category) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }
        if (lastFourDigits == null || lastFourDigits.length() != 4) {
            throw new IllegalArgumentException("Se requieren exactamente los últimos 4 dígitos.");
        }
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La tarjeta está vencida.");
        }

        em.getTransaction().begin();
        try {
            PaymentMethod pm = new PaymentMethod();
            pm.setUser(user);
            pm.setAlias(alias);
            pm.setLastFourDigits(lastFourDigits);
            pm.setCardholderName(cardholderName);
            pm.setExpirationDate(expirationDate);
            pm.setPaymentCategory(category);

            // Si es el primero, marcarlo como default
            List<PaymentMethod> existing = paymentMethodDAO.findByUserId(userId);
            pm.setDefaultMethod(existing.isEmpty());

            paymentMethodDAO.create(pm);
            em.getTransaction().commit();
            return pm;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public PaymentMethod findById(Long id) {
        return paymentMethodDAO.findById(id);
    }

    @Override
    public List<PaymentMethod> findByUser(Long userId) {
        return paymentMethodDAO.findByUserId(userId);
    }

    @Override
    public void setDefault(Long paymentMethodId, Long userId) {
        em.getTransaction().begin();
        try {
            List<PaymentMethod> methods = paymentMethodDAO.findByUserId(userId);
            for (PaymentMethod pm : methods) {
                if (pm.isDefaultMethod()) {
                    pm.setDefaultMethod(false);
                    paymentMethodDAO.update(pm);
                }
            }
            PaymentMethod target = paymentMethodDAO.findById(paymentMethodId);
            if (target == null || !target.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Método de pago no válido para este usuario.");
            }
            target.setDefaultMethod(true);
            paymentMethodDAO.update(target);

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        try {
            paymentMethodDAO.delete(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
