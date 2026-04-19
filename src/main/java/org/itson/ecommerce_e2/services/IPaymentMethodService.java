package org.itson.ecommerce_e2.services;

import java.time.LocalDate;
import java.util.List;
import org.itson.ecommerce_e2.models.PaymentMethod;
import org.itson.ecommerce_e2.models.enums.PaymentCategory;

public interface IPaymentMethodService {

    PaymentMethod add(Long userId, String alias, String lastFourDigits,
            String cardholderName, LocalDate expirationDate,
            PaymentCategory category);

    PaymentMethod findById(Long id);

    List<PaymentMethod> findByUser(Long userId);

    /**
     * Marca este método como predeterminado y quita el flag a los demás.
     * @param paymentMethodId
     * @param userId
     */
    void setDefault(Long paymentMethodId, Long userId);

    void delete(Long id);
}
